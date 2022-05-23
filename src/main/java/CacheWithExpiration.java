import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheWithExpiration<K, V> {

  private static final long DEFAULT_EXPIRATION_TIME = 30000L;
  private final ConcurrentMap<K, CacheWithExpiration.Entry<V>> map;
  private volatile AtomicInteger queryCount = new AtomicInteger(0);
  private volatile int QUERY_OVERFLOW = 300;

  public CacheWithExpiration() {
    this.map = new ConcurrentHashMap<>();
  }

  public V get(K key) {
    if (queryCount.incrementAndGet() >= QUERY_OVERFLOW) {
      cleanup();
    }
    Entry<V> entry = entryFor(key);
    return entry == null ? null : entry.val();
  }

  private Entry<V> entryFor(K key) {
    Entry<V> entry = this.map.get(key);
    if (entry != null) {
      long deltaTime = System.currentTimeMillis() - entry.timestamp();
      if (deltaTime < 0L || deltaTime > entry.expirationTime()) {
        System.out.println("Removing value " + entry.val + " expired after " + entry.expirationTime() + " ms");
        this.map.remove(key);
        entry = null;
      }
    }

    return entry;
  }

  private void cleanup() {
    Set<K> keySet = map.keySet();
    // Avoid ConcurrentModificationExceptions
    List<K> keys = new ArrayList<>(keySet.size());
    int i = 0;
    for (K key : keySet) {
      keys.set(i++, key);
    }
    for (K key : keys) {
      entryFor(key);
    }
    queryCount.getAndSet(0);
  }

  public synchronized void put(K key, V val) {
    if (queryCount.incrementAndGet() >= QUERY_OVERFLOW) {
      cleanup();
    }
    Entry<V> entry = entryFor(key);
    if (entry != null) {
      entry.setTimestamp(System.currentTimeMillis());
      entry.setVal(val);
    } else {
      this.map.put(key, new Entry<>(val, DEFAULT_EXPIRATION_TIME));
    }
  }

  public synchronized void put(K key, V val, long expirationTime) {
    if (queryCount.incrementAndGet() >= QUERY_OVERFLOW) {
      cleanup();
    }
    Entry<V> entry = entryFor(key);
    if (entry != null) {
      entry.setTimestamp(System.currentTimeMillis());
      entry.setVal(val);
      entry.setMillisecBeforeExpiration(expirationTime);
    } else {
      this.map.put(key, new Entry<>(val, expirationTime));
    }
  }

  synchronized void clear() {
    this.map.clear();
  }

  static class Entry<V> {

    private V val;
    private long timestamp;
    private long millisecBeforeExpiration;

    private Entry(V val, long millisecBeforeExpiration) {
      this.val = val;
      this.timestamp = System.currentTimeMillis();
      this.millisecBeforeExpiration = millisecBeforeExpiration;
    }

    public V val() {
      return val;
    }

    public void setVal(V val) {
      this.val = val;
    }

    public long expirationTime() {
      return millisecBeforeExpiration;
    }

    public void setMillisecBeforeExpiration(long millisecBeforeExpiration) {
      this.millisecBeforeExpiration = millisecBeforeExpiration;
    }

    public long timestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }
  }
}
