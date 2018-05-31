package club.bytecode.the.jda.decompilers.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DecompileFilters {
    public static final Map<String, DecompileFilter> BY_NAME = new LinkedHashMap<>();
    
    public static Collection<DecompileFilter> getAllFilters() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }
    
    public static DecompileFilter getByName(String name) {
        return BY_NAME.get(name);
    }
}
