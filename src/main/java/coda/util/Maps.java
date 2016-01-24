package coda.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class Maps {
    public static <K, V> Map<K, V> map(MapEntry<K, V>... entries) {
        Map<K, V> map = new HashMap<>();
        for (MapEntry<K, V> entry : entries) {
            map.put(entry.key, entry.value);
        }
        return map;
    }

    public static MapEntry<String, Object> e(String key, Object value) {
        return new MapEntry<>(key, value);
    }


    public static class MapEntry<K, V> {
        public final K key;
        public final V value;

        public MapEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}
