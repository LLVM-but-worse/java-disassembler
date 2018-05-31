package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.decompilers.bytecode.BytecodeDecompiler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Decompilers {
    private static final Map<String, JDADecompiler> BY_NAME = new LinkedHashMap<>();

    public final static JDADecompiler PROCYON = new ProcyonDecompiler();
    public final static JDADecompiler CFR = new CFRDecompiler();
    public final static JDADecompiler FERNFLOWER = new FernflowerDecompiler();
    public final static JDADecompiler BYTECODE = new BytecodeDecompiler();

    public static void registerDecompiler(JDADecompiler decompiler) {
        BY_NAME.put(decompiler.getFullName(), decompiler);
    }

    public static Collection<JDADecompiler> getAllDecompilers() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
    
    /**
     * @param name the FULL name of the decompiler
     * @return the decompiler, if found
     */
    public static JDADecompiler getByName(String name) {
        return BY_NAME.get(name);
    }
    
    static
    {
        registerDecompiler(PROCYON);
        registerDecompiler(CFR);
        registerDecompiler(FERNFLOWER);
        registerDecompiler(BYTECODE);
    }
}
