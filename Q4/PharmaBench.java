import java.util.*;
class Med { final String code; int stock; Med(String c,int s){code=c;stock=s;} }
public class PharmaBench {
    static long sink;
    public static void main(String[] a){
        System.out.println("n,map,put_ms,100k_lookups_ms,ns_per_lookup,iterate_ms,report_code_order_ms,report_insertion_order_ms,code_order_ok,insertion_order_ok");
        for(int n:new int[]{1000,10000,100000}){
            String[] codes=new String[n]; Random r=new Random(7);
            Set<String> used=new LinkedHashSet<>(); while(used.size()<n) used.add("M"+(100000+r.nextInt(900000)));
            codes=used.toArray(new String[0]);
            String[] sortedCodes=codes.clone(); Arrays.sort(sortedCodes);
            String[] look=new String[100000]; for(int i=0;i<look.length;i++) look[i]=codes[r.nextInt(n)];
            for(String kind:new String[]{"HashMap","LinkedHashMap","TreeMap"}){
                double put=0,lk=0,it=0,rc=0,ri=0; boolean co=false,io=false; int R=5;
                for(int run=0;run<R+2;run++){
                    Map<String,Med> m= kind.equals("HashMap")? new HashMap<>(): kind.equals("TreeMap")? new TreeMap<>(): new LinkedHashMap<>();
                    long t0=System.nanoTime(); for(String c:codes) m.put(c,new Med(c,10)); long t1=System.nanoTime();
                    long s=0; for(String c:look) s+=m.get(c).stock; sink+=s; long t2=System.nanoTime();
                    s=0; for(Med md:m.values()) s+=md.stock; sink+=s; long t3=System.nanoTime();
                    // report in code order
                    List<String> k1= kind.equals("TreeMap")? new ArrayList<>(m.keySet()): sortedFrom(m);
                    long t4=System.nanoTime();
                    // report in insertion order
                    List<String> k2= kind.equals("LinkedHashMap")? new ArrayList<>(m.keySet()): insertionFrom(m,codes);
                    long t5=System.nanoTime();
                    if(run>=2){put+=(t1-t0)/1e6; lk+=(t2-t1)/1e6; it+=(t3-t2)/1e6; rc+=(t4-t3)/1e6; ri+=(t5-t4)/1e6;}
                    co=k1.equals(Arrays.asList(sortedCodes)); io=k2.equals(Arrays.asList(codes));
                }
                System.out.printf("%d,%s,%.2f,%.2f,%.1f,%.3f,%.3f,%.3f,%s,%s%n",n,kind,put/R,lk/R,lk/R*1e6/100000,it/R,rc/R,ri/R,co,io);
            }
            // sorted array + binary search
            double bs=0,ins=0; int R=5; 
            for(int run=0;run<R+2;run++){
                Med[] arr=new Med[n]; for(int i=0;i<n;i++) arr[i]=new Med(sortedCodes[i],10);
                Comparator<Med> cmp=Comparator.comparing(md->md.code);
                long t0=System.nanoTime(); long s=0; for(String c:look){ int i=Arrays.binarySearch(arr,new Med(c,0),cmp); s+=arr[i].stock;} sink+=s; long t1=System.nanoTime();
                if(run>=2) bs+=(t1-t0)/1e6;
            }
            // insertion cost in sorted array: 1000 inserts with shifting
            Med[] arr=new Med[n+1000]; for(int i=0;i<n;i++) arr[i]=new Med(sortedCodes[i],10); int cnt=n;
            long t0=System.nanoTime();
            for(int i=0;i<1000;i++){ Med nm=new Med("M"+(1000000+r.nextInt(900000)),1); int p=Arrays.binarySearch(arr,0,cnt,nm,Comparator.comparing((Med q)->q.code)); if(p<0)p=-p-1; System.arraycopy(arr,p,arr,p+1,cnt-p); arr[p]=nm; cnt++; }
            ins=(System.nanoTime()-t0)/1e6;
            System.out.printf("%d,SortedArray+binarySearch,-,%.2f,%.1f,-,0 (already sorted),-,-,-  | 1000 inserts: %.2f ms%n",n,bs/R,bs/R*1e6/100000,ins);
        }
    }
    static List<String> sortedFrom(Map<String,Med> m){ List<String> l=new ArrayList<>(m.keySet()); Collections.sort(l); return l; }
    static List<String> insertionFrom(Map<String,Med> m,String[] codes){ return Arrays.asList(codes); } 
}
