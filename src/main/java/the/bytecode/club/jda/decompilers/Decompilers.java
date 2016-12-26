package the.bytecode.club.jda.decompilers;

import the.bytecode.club.jda.decompilers.bytecode.ClassNodeDecompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Decompilers {
    static final List<Decompiler> BY_NAME = new ArrayList<>();

    public final static Decompiler PROCYON = new ProcyonDecompiler();
    public final static Decompiler CFR = new CFRDecompiler();
    public final static Decompiler FERNFLOWER = new FernflowerDecompiler();
    public final static Decompiler BYTECODE = new ClassNodeDecompiler();


    public static Collection<Decompiler> getAllDecompilers() {
        return Collections.unmodifiableCollection(BY_NAME);
    }
}
