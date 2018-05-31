package club.bytecode.the.jda.decompilers.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DecompileFilters {
    public static final Map<String, DecompileFilter> BY_NAME = new LinkedHashMap<>();
    
    public static void registerFilter(DecompileFilter filter) {
        BY_NAME.put(filter.getFullName(), filter);
    }
    
    public static Collection<DecompileFilter> getAllFilters() {
        return Collections.unmodifiableCollection(BY_NAME.values());
    }

    /**
     * @param name the FULL name of the decompile filter
     * @return the filter, if found
     */
    public static DecompileFilter getByName(String name) {
        return BY_NAME.get(name);
    }
}
