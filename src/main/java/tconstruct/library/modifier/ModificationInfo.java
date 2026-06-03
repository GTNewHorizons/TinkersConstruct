package tconstruct.library.modifier;

import com.github.bsideup.jabel.Desugar;

@Desugar
public record ModificationInfo(int total, int[] toRemove) {}
