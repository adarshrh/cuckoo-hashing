package axh190002;

import java.util.Timer;
import java.util.*;

import static java.lang.System.exit;

public class CuckooHash < K extends Comparable< ? super K> ,V extends Comparable< ? super V>> {
    private int size=0;
    private int capacity=16;
    private float loadFactor=0.5f;
    private int threshold;
    private Entry<K,V> hashTable[][] = new Entry[2][capacity];
    private Set<V> secondary = new HashSet<>();

    static class Entry< K extends Comparable< ? super K> ,V extends Comparable< ? super V>>{
        K key;
        V value;

        public Entry(K key, V value){
            this.key = key;
            this.value = value;
        }

        /**
         * overriding equals method to compare the objects using their keys
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            if(obj instanceof Entry){
                if(((Entry) obj).key.compareTo(this.key) == 0){
                    return true;
                }
            }
            else{
                if(key.compareTo((K)obj) ==0){
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Assigning capacity and LoadFactor
     * @param capacity
     * @param loadFactor
     */
    CuckooHash(int capacity, float loadFactor){
        this.capacity = capacity;
        this.loadFactor = loadFactor;
        threshold = (int) Math.floor(Math.log(capacity*2));
        hashTable = new Entry[2][capacity];
        System.out.println("threshold is "+Math.log(capacity));
    }

    /**
     * hashcode for hashtable 1
     * @param value
     * @return
     */
    int hash1(V value){
        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        int h = value.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        h = h ^ (h >>> 7) ^ (h >>> 4);
        return Math.abs(h%capacity);
    }

    /**
     * hashcode for hashtable 2
     * @param value
     * @return
     */
    int hash2(V value){
        int hashValue = value.hashCode();
        //int key =  (hashValue/capacity)%capacity;
        int key = hash1(value);
        key += (1+hashValue%9);
        key = key%capacity;
        return Math.abs(key);
    }

    /**
     * adds the new value in the hashtable using cuckoo hashing algorithm
     * @param value
     * @return
     */
    Entry add(V value){
        float load = (float) size/(capacity+capacity);
        if(load >= loadFactor)
            reSize();
        int key=0;
        if(secondary.contains(value)){
            return new Entry(0,value);
        }

        for(int i=0;i<threshold;i++){
            int hashFnNumber = i%2;
            key = hashFnNumber==0 ? hash1(value) : hash2(value);
            Entry entry = hashTable[hashFnNumber][key];

            if(entry==null){
                entry = new Entry(key,value);
                // System.out.println("Writing value:"+entry.value.toString()+" to table:"+hashFnNumber);
                hashTable[hashFnNumber][key] = entry;
                size++;
                return entry;
            } else if(value.equals(entry.value))
                return entry;

            entry = new Entry(key,value);
            value = hashTable[hashFnNumber][key].value;
            hashTable[hashFnNumber][key] = entry;
        }
        Entry entry = new Entry(key,value);
        secondary.add(value);
        size++;
        return entry;
    }

    /**
     * Check if the hashtable 1, 2 or seconday hashtable contains the value
     * @param value
     * @return boolean
     */
    boolean contains(V value){
        int loc = hash1(value);
        if(hashTable[0][loc]!=null){
            if(hashTable[0][loc].value.equals(value)){
                return true;
            }

        }

        loc = hash2(value);
        if(hashTable[1][loc]!=null){
            if(hashTable[1][loc].value.equals(value)){
                return true;
            }

        }

        if(secondary.contains(value)){
            return true;
        }

        return false;
    }

    /**
     * check if the value is present in the hashtables and if so delete it
     * @param value
     * @return boolean
     */
    boolean remove(V value){
        int loc = hash1(value);
        if(hashTable[0][loc]!=null){
            if(hashTable[0][loc].equals(value)){
                hashTable[0][loc] = null;
                size--;
                return true;
            }

        }
        loc = hash2(value);
        if(hashTable[1][loc]!=null){
            if(hashTable[1][loc].equals(value)){
                hashTable[1][loc] = null;
                size--;
                return true;
            }
        }
        else{
            if(secondary.contains(value)){
                secondary.remove(value);
                size--;
                return true;
            }
        }
        return false;
    }

    /**
     * if the hashtable size has exceeds the loadfactor then double the size of the hashtable, rehash and store the values in resized hashtables
     */
    private void reSize() {
        Entry[][] oldTable = hashTable;
        hashTable = new Entry[2][oldTable[0].length*2];
        capacity = oldTable[0].length*2;
        threshold = (int) Math.floor(Math.log(capacity));
        Set<V> oldSecondary = new HashSet<>(secondary);
        secondary.clear();
        size=0;
        for(int i=0;i<oldTable[0].length;i++){

            if(oldTable[0][i] != null){
                V value = (V) oldTable[0][i].value;
                add(value);
            }

            if(oldTable[1][i] != null){
                V value = (V) oldTable[1][i].value;
                add(value);
            }

        }
        Iterator<V> itr = oldSecondary.iterator();
        while (itr.hasNext()){
            add(itr.next());
        }
    }

    /**
     * Prints hashtable 1, 2 nd secondary hashtable
     */
    public void printMap(){
        System.out.println();
        System.out.print("Table 1: [ ");
        for(int i=0;i<hashTable[0].length;i++){
            if(hashTable[0][i]!=null)
                System.out.print(hashTable[0][i].value.toString()+" ");
        }
        System.out.print(" ] ");
        System.out.println();
        System.out.print("Table 2: [ ");
        for(int i=0;i<hashTable[1].length;i++){
            if(hashTable[1][i]!=null)
                System.out.print(hashTable[1][i].value.toString()+" ");
        }
        System.out.print(" ] ");
        System.out.println();
        System.out.println("secondary:"+secondary.toString());
    }

    /**
     * set is populated with 10000000 randomly generated numbers
     * @param count
     * @return set
     */
    public static Set<Integer> generateRandom(int count){
        Integer[] arr = new Integer[count];
        Random random = new Random(10000000);
        Set<Integer> set = new HashSet<>();
        for(int i=0;i<count;i++){
            arr[i] = random.nextInt() & Integer.MAX_VALUE;
            set.add(arr[i]);
        }

        return set;
    }
    public static void main(String args[]){
        CuckooHash<Integer,Integer> map = new CuckooHash<Integer, Integer>(16,0.9f);

        Set<Integer> set =  generateRandom(10000000);        //set is initially populated with 10000000 randomly generated numbers
        Iterator<Integer> itr = set.iterator();
        Timer timer = new Timer();

        Set<Integer> hashset = new HashSet<Integer>(16,0.9f);  // java implemented hashset is used
        System.out.println("Load Factor: "+map.loadFactor);

        System.out.println("CuckooHashMap Vs Java Implemented HashSet");
        while(true) {
            System.out.println();
            System.out.println("Enter 1: Add 2: Search 3: Remove 0: Exit");
            Scanner scanner = new Scanner(System.in);
            int ch = scanner.nextInt();
            switch (ch) {
                case 1:
                    itr = set.iterator();
                    timer.start();
                    while (itr.hasNext()){
                        map.add(itr.next());
                    }
                    timer.end();
                    System.out.println("Map using CuckooHash "+timer.toString());

                    itr = set.iterator();
                    timer.start();
                    while (itr.hasNext()){
                        hashset.add(itr.next());
                    }
                    timer.end();
                    System.out.println("Distinct val: "+hashset.size());
                    System.out.println("Java HashSet "+timer.toString());
                    break;
                case 2:
                    itr = set.iterator();
                    Timer searchtimer = new Timer();
                    while (itr.hasNext()){
                        map.contains(itr.next());
                    }
                    searchtimer.end();
                    System.out.println("Map using CuckooHash "+searchtimer.toString());

                    itr = set.iterator();
                    searchtimer.start();
                    while (itr.hasNext()){
                        hashset.contains(itr.next());
                    }
                    searchtimer.end();
                    System.out.println("Java HashSet "+searchtimer.toString());
                    break;
                case 3:
                    itr = set.iterator();
                    Timer removetimer = new Timer();
                    while (itr.hasNext()){
                        map.remove(itr.next());
                    }
                    removetimer.end();
                    System.out.println("Map using CuckooHash "+removetimer.toString());

                    itr = set.iterator();
                    removetimer.start();
                    while (itr.hasNext()){
                        hashset.remove(itr.next());
                    }
                    removetimer.end();
                    System.out.println("Java HashSet "+removetimer.toString());
                    break;
                case 0:
                    exit(0);
            }
        }

    }

}
