package club.bytecode.the.jda.decompilers;

import club.bytecode.the.jda.decompilers.bytecode.BytecodeDecompiler;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Decompilers {
    public static final Map<String, JDADecompiler> BY_NAME = new LinkedHashMap<>();

    public final static JDADecompiler PROCYON = new ProcyonDecompiler();
    public final static JDADecompiler CFR = new CFRDecompiler();
    public final static JDADecompiler FERNFLOWER = new FernflowerDecompiler();
    public final static JDADecompiler BYTECODE = new BytecodeDecompiler();


    public static Collection<JDADecompiler> getAllDecompilers() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
    
    public static JDADecompiler getByName(String name) {
        return BY_NAME.get(name);
    }
    
    static
    {
        Decompilers.BY_NAME.put(PROCYON.getName(), PROCYON);
        Decompilers.BY_NAME.put(CFR.getName(), CFR);
        Decompilers.BY_NAME.put(FERNFLOWER.getName(), FERNFLOWER);
        Decompilers.BY_NAME.put(BYTECODE.getName(), BYTECODE);
    }
}
