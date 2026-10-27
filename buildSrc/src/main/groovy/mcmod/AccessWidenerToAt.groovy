package mcmod

// Converts a Fabric access widener (or class tweaker) into a NeoForge access transformer, so a mod declares
// its access changes once in shared code. Minecraft 26.x is unobfuscated, so names carry over unchanged.
class AccessWidenerToAt {
    // Access transformer modifier for each kind of target and set of access widener keywords.
    private static final Map<String, Map<Set<String>, String>> MODIFIERS = [
            class : [(['accessible'] as Set): 'public', (['extendable'] as Set): 'public-f', (['accessible', 'extendable'] as Set): 'public-f'],
            method: [(['accessible'] as Set): 'public', (['extendable'] as Set): 'protected-f', (['accessible', 'extendable'] as Set): 'public-f'],
            field : [(['accessible'] as Set): 'public', (['mutable'] as Set): 'private-f', (['accessible', 'mutable'] as Set): 'public-f'],
    ]

    static List<String> convert(List<String> awLines) {
        // Access transformer target -> [kind, keywords, first line number], in first-seen order
        Map<String, Map> targets = new LinkedHashMap<>()
        awLines.eachWithIndex { String raw, int index ->
            def line = raw.replaceFirst(/#.*/, '').trim()
            if (!line) return
            def parts = line.split(/\s+/)
            if (parts[0] in ['accessWidener', 'classTweaker']) return // header, e.g. "classTweaker v1 official"
            def keyword = parts[0].replaceFirst(/^transitive-/, '')
            def kind = parts.length > 1 ? parts[1] : ''
            def expectedParts = [class: 3, method: 5, field: 5][kind]
            if (!(keyword in ['accessible', 'extendable', 'mutable']) || parts.length != expectedParts) {
                throw new IllegalArgumentException("Access widener line ${index + 1}: can't convert '${raw.trim()}'")
            }
            def owner = parts[2].replace('/', '.')
            def target = kind == 'class' ? owner : kind == 'method' ? "${owner} ${parts[3]}${parts[4]}" : "${owner} ${parts[3]}"
            def entry = targets.computeIfAbsent(target.toString()) { [kind: kind, keywords: [] as Set, line: index + 1] }
            entry.keywords << keyword
        }
        targets.collect { target, entry ->
            def modifier = MODIFIERS[entry.kind][entry.keywords]
            if (!modifier) {
                throw new IllegalArgumentException("Access widener line ${entry.line}: ${entry.keywords.sort().join(' + ')} isn't valid for a ${entry.kind}")
            }
            "${modifier} ${target}".toString()
        }
    }
}
