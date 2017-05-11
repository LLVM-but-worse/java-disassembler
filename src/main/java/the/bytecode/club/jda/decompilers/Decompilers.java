package the.bytecode.club.jda.decompilers;

import the.bytecode.club.jda.decompilers.bytecode.ClassNodeDecompiler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Decompilers {
    static final Map<String, Decompiler> BY_NAME = new LinkedHashMap<>();

    public final static Decompiler PROCYON = new FernflowerDecompiler(); //TODo
    public final static Decompiler CFR = new CFRDecompiler();
    public final static Decompiler FERNFLOWER = new FernflowerDecompiler();
    public final static Decompiler BYTECODE = new ClassNodeDecompiler();


    public static Collection<Decompiler> getAllDecompilers() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
}
