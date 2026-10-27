package mcmod

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertThrows

class AccessWidenerToAtTest {
    @Test
    void classAccessibleAndExtendable() {
        assert AccessWidenerToAt.convert(['accessWidener v2 official', 'accessible class net/minecraft/A', 'extendable class net/minecraft/A']) == ['public-f net.minecraft.A']
    }

    @Test
    void methodAndFieldMapping() {
        assert AccessWidenerToAt.convert(['accessWidener v2 official', '# c', '', 'accessible method net/minecraft/A m (I)V', 'accessible field net/minecraft/A f I', 'mutable field net/minecraft/A g I']) ==
                ['public net.minecraft.A m(I)V', 'public net.minecraft.A f', 'private-f net.minecraft.A g']
    }

    @Test
    void extendableMethodAndMergedEntries() {
        assert AccessWidenerToAt.convert(['accessWidener v2 official', 'extendable method net/minecraft/A m (I)V', 'extendable method net/minecraft/A n ()V', 'accessible method net/minecraft/A n ()V', 'accessible field net/minecraft/A f I', 'mutable field net/minecraft/A f I']) ==
                ['protected-f net.minecraft.A m(I)V', 'public-f net.minecraft.A n()V', 'public-f net.minecraft.A f']
    }

    @Test
    void transitiveAndClassTweakerHeader() {
        assert AccessWidenerToAt.convert(['classTweaker v1 official', 'transitive-accessible field net/minecraft/A f I']) == ['public net.minecraft.A f']
    }

    @Test
    void tabSeparatedHeaderAndEntries() {
        assert AccessWidenerToAt.convert(['accessWidener\tv2\tnamed', 'accessible\tfield\tnet/minecraft/A\tf\tI']) == ['public net.minecraft.A f']
    }

    @Test
    void unknownKeywordReportsLine() {
        def e = assertThrows(IllegalArgumentException) { AccessWidenerToAt.convert(['accessWidener v2 official', 'visible class net/minecraft/A']) }
        assert e.message.contains('line 2')
    }
}
