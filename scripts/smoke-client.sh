#!/bin/bash
# Launches a mod in the game for one or more <minecraft>-<loader> targets, checks the game log, then closes the game.
# Usage: scripts/smoke-client.sh <mod_id> [target ...] [--server] [--expect TEXT ...]
#   No targets = every target in mods/<mod_id>/gradle.properties.
#   TEXT may contain {loader} and {minecraft}. A client must also reach the main menu ("Sound engine started").
set -uo pipefail
cd "$(dirname "$0")/.."

usage="usage: $0 <mod_id> [target ...] [--server] [--expect TEXT ...]"
mod=${1:?$usage}
shift
targets=()
expects=()
task=runClient
while (($#)); do
    case $1 in
        --server) task=runServer; shift ;;
        --expect) expects+=("${2:?--expect needs text}"); shift 2 ;;
        *) targets+=("$1"); shift ;;
    esac
done
if ((${#targets[@]} == 0)); then
    IFS=',' read -ra targets <<< "$(sed -n 's/^targets=//p' "mods/$mod/gradle.properties")"
fi
[[ $task == runClient ]] && expects+=("Sound engine started")

progress_file=~/.claude/progress/$$.json
mkdir -p ~/.claude/progress build/smoke
started=$(date +%s)
trap 'rm -f "$progress_file" "$progress_file.tmp"' EXIT
progress() {
    printf '{"label":"Smoke test %s","done":%s,"total":%s,"unit":"targets","note":"%s","pid":%s,"started":%s,"updated":%s}' \
        "$mod" "$1" "${#targets[@]}" "$2" $$ "$started" "$(date +%s)" > "$progress_file.tmp" && mv "$progress_file.tmp" "$progress_file"
}
game_processes() { pgrep -f 'net\.neoforged\.devlaunch\.Mai[n]|net\.fabricmc\.devlaunchinjector\.Mai[n]'; }

failures=0
finished=0
for target in "${targets[@]}"; do
    target=${target//[[:space:]]/}
    minecraft=${target%%-*}
    loader=${target#*-}
    game_log="mods/$mod/versions/$target/run/logs/latest.log"
    gradle_log="build/smoke/$mod-$target-$task.log"
    wanted=()
    for text in "${expects[@]}"; do
        text=${text//\{loader\}/$loader}
        wanted+=("${text//\{minecraft\}/$minecraft}")
    done

    # A fresh game folder opens Minecraft's one-time accessibility setup instead of the title screen;
    # turn it off so client checks always reach the title screen.
    if [[ $task == runClient ]]; then
        options="mods/$mod/versions/$target/run/options.txt"
        mkdir -p "$(dirname "$options")"
        if [[ -f $options ]]; then
            sed -i '' 's/^onboardAccessibility:true$/onboardAccessibility:false/' "$options"
        else
            echo "onboardAccessibility:false" > "$options"
        fi
    fi

    launched=$(date +%s)
    ./gradlew ":$mod:$target:$task" --console=plain > "$gradle_log" 2>&1 &
    gradle_pid=$!
    missing=("${wanted[@]}")
    result=""
    while [[ -z $result ]]; do
        # Check whether Gradle is still running before reading the log, so a game that logs
        # everything and then exits on its own (e.g. a server stopping at the EULA check) still passes.
        gradle_running=true
        kill -0 "$gradle_pid" 2>/dev/null || gradle_running=false
        if [[ -f $game_log ]] && (($(stat -f %B "$game_log") >= launched)); then
            still=()
            for text in "${missing[@]}"; do
                grep -qF -- "$text" "$game_log" || still+=("$text")
            done
            missing=("${still[@]+"${still[@]}"}")
            ((${#missing[@]} == 0)) && result=ok
        fi
        if [[ -z $result ]]; then
            if ! $gradle_running; then
                result="Gradle exited before the game was ready"
            elif (($(date +%s) - launched > 600)); then
                result="timed out after 600 s"
            fi
        fi
        progress "$finished" "$target: waiting for ${missing[0]:-game}"
        [[ -z $result ]] && sleep 2
    done

    kill "$gradle_pid" 2>/dev/null
    wait "$gradle_pid" 2>/dev/null
    for _ in $(seq 30); do
        game_processes > /dev/null || break
        sleep 1
    done

    if [[ $result == ok ]]; then
        echo "PASS $target"
    else
        failures=$((failures + 1))
        echo "FAIL $target: $result; missing: ${missing[*]:-nothing}"
        if [[ -f $game_log ]] && (($(stat -f %B "$game_log") >= launched)); then tail -30 "$game_log"; else tail -30 "$gradle_log"; fi
    fi
    if game_processes > /dev/null; then
        failures=$((failures + 1))
        echo "FAIL $target: a game process is still running after Gradle was stopped"
    fi
    finished=$((finished + 1))
done
exit $((failures > 0))
