package io.github.rebelkeithy.dualhotbar;

// The two hotbars ride on opposite ends of a diameter of a turning wheel. A bar's angle on the wheel gives both its
// scale (the vertical coordinate, sin) and the height of its centre above the bottom of the screen (the horizontal
// coordinate, cos). At rest the selectable hotbar sits at 135° (scale 1, at the bottom) and the second bar at 315°
// (the configured scale, above it). Scrolling down turns the wheel 180° counterclockwise, so the hotbar dips, shrinks
// past the second bar's size and rises into its place, while the second bar lifts, grows past full size in front of it
// and settles at the bottom. Scrolling up turns it clockwise, which plays the same motion in reverse. See
// mods/dualhotbar/README.md for the diagram this follows.
public final class SwapAnimation {
    public static final double HOTBAR_ANGLE = 135.0;
    public static final double SECOND_BAR_ANGLE = HOTBAR_ANGLE + 180.0;

    // Degrees the wheel still had to turn when the current animation started; 0 when at rest, negative when it is
    // turning clockwise.
    private static double startLag;
    private static long startNanos;

    private SwapAnimation() {
    }

    // The inventory has just swapped: the items now in the hotbar are drawn where the second bar was, and the wheel
    // turns them into place. HotbarSwapper doesn't start a swap while one is animating.
    public static void start(boolean wheelDown) {
        startLag = wheelDown ? 180.0 : -180.0;
        startNanos = System.nanoTime();
    }

    public static boolean isAnimating() {
        return lag() != 0.0;
    }

    // Degrees the wheel is behind (positive) or ahead of (negative) its resting position. Subtract this from a bar's
    // resting angle to get its angle now.
    public static double lag() {
        int millis = DualHotbarConfig.swapAnimationMillis();
        if (startLag == 0.0 || millis <= 0) {
            startLag = 0.0;
            return 0.0;
        }
        double t = (System.nanoTime() - startNanos) / (millis * 1_000_000.0);
        if (t >= 1.0) {
            startLag = 0.0;
            return 0.0;
        }
        // Sine ease-in-out, so the wheel starts and stops smoothly instead of at full speed.
        double eased = (1.0 - Math.cos(Math.PI * t)) / 2.0;
        return startLag * (1.0 - eased);
    }

    // Scale and centre height of a bar at angleDegrees on the wheel. Measured from the hotbar's resting angle, the
    // resting poses sit at 0° and 180° and the wheel's sides at ±90°. On a circle the sides would be as far apart in
    // height as the resting poses; when the bars rest closer together than that (a large overlap), the sides are pushed
    // out to minLift, so the bars still separate mid-swap instead of only changing size in place.
    public static Pose pose(double angleDegrees, float secondScale, float hotbarCenter, float secondCenter, float minLift) {
        double fromHotbar = Math.toRadians(angleDegrees - HOTBAR_ANGLE);
        double cos = Math.cos(fromHotbar);
        double sin = Math.sin(fromHotbar);
        double scaleMiddle = (1.0 + secondScale) / 2.0;
        double scaleHalfGap = (1.0 - secondScale) / 2.0;
        double heightMiddle = (hotbarCenter + secondCenter) / 2.0;
        double heightHalfGap = (secondCenter - hotbarCenter) / 2.0;
        double lift = Math.max(heightHalfGap, minLift);
        return new Pose(
                (float) (scaleMiddle + scaleHalfGap * (cos - sin)),
                (float) (heightMiddle - heightHalfGap * cos - lift * sin)
        );
    }

    // centerHeight is measured up from the bottom of the screen, in GUI pixels.
    public record Pose(float scale, float centerHeight) {
    }
}
