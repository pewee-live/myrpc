package com.pewee.rpc.servicemanage;



import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;



public class LRUCache<K, V>
  extends LinkedHashMap<K, V>
{
  private static final long serialVersionUID = -5167631809472116969L;
  private static final float DEFAULT_LOAD_FACTOR = 0.75F;
  private static final int DEFAULT_MAX_CAPACITY = 1000;
  private volatile int maxCapacity;
  private final Lock lock = new ReentrantLock();
  
  public LRUCache() {
    this(1000);
  }
  
  public LRUCache(int maxCapacity) {
    super(16, 0.75F, true);
    this.maxCapacity = maxCapacity;
  }
  
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
  {
    return size() > maxCapacity;
  }
  
  public boolean containsKey(Object key)
  {
    try {
      lock.lock();
      return super.containsKey(key);
    } finally {
      lock.unlock();
    }
  }
  
  public V get(Object key)
  {
    try {
      lock.lock();
      return (V)super.get(key);
    } finally {
      lock.unlock();
    }
  }
  
  public V put(K key, V value)
  {
    try {
      lock.lock();
      return (V)super.put(key, value);
    } finally {
      lock.unlock();
    }
  }
  
  public V remove(Object key)
  {
    try {
      lock.lock();
      return (V)super.remove(key);
    } finally {
      lock.unlock();
    }
  }
  
  public int size()
  {
    try {
      lock.lock();
      return super.size();
    } finally {
      lock.unlock();
    }
  }
  
  public void clear()
  {
    try {
      lock.lock();
      super.clear();
    } finally {
      lock.unlock();
    }
  }
  
  public int getMaxCapacity() {
    return maxCapacity;
  }
  
  public void setMaxCapacity(int maxCapacity) {
    this.maxCapacity = maxCapacity;
  }
}
