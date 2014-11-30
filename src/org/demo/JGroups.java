package org.demo;

/**
 * @author Bela Ban
 * @since x.y
 */
public class JGroups {
    public static JGroupsInstance newJGroupsInstance(String config) throws Exception {
        return new JGroupsInstance(config);
    }

    public static JGroupsInstance newJGroupsInstance(String config, String cluster, String name) throws Exception {
        return new JGroupsInstance(config, cluster, name);
    }
}
