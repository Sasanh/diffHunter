/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package diffhunter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.sleepycat.bind.ByteArrayBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.OperationStatus;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author hashemis
 */
public class BerkeleyDB_Box 
{/*        public static Dictionary<int, int> Get_Coord_Read(HashDatabase hashdb_first, string gene_of_interest)
        {
            Dictionary<int, int> first_ = new Dictionary<int, int>();
            if (!hashdb_first.Exists(Get_BDB(gene_of_interest))) return new Dictionary<int, int>();
            first_ = Get_Dictionary(hashdb_first.Get(Get_BDB(gene_of_interest)).Value);
            first_ = first_.OrderBy(x => x.Key).ToDictionary(x => x.Key, x => x.Value);

            return first_;
        }*/
    public static Map<Integer,Integer>Get_Coord_Read(Database hashdb_first,String gene_of_interest) throws IOException, ClassNotFoundException
    {
         //Map<Integer,Integer> first_=new HashMap<>();
         DatabaseEntry key=Get_BDB(gene_of_interest);
         DatabaseEntry data=new DatabaseEntry();
         //System.out.println("Database key count:\t"+hashdb_first.count());
        if(OperationStatus.NOTFOUND==hashdb_first.get(null, key, data, null)){return Collections.EMPTY_MAP;}//  new HashMap<>();}
        //first_=
        //HashMap<String,String> converted_= (HashMap<String,String>)(deserialize(binding.entryToObject(entry3)))
          ByteArrayBinding binding=new  ByteArrayBinding();
         return (Map<Integer,Integer>)deserialize(binding.entryToObject(data));
        //return new HashMap<>();
    }
    /*        public static HashDatabase Get_BerkeleyDB(string folder_name, string name_)
        {
            HashDatabaseConfig hashconfig = new HashDatabaseConfig()
            {
                //1000000000
                Duplicates = DuplicatesPolicy.NONE,
                Creation = CreatePolicy.IF_NEEDED,
                CacheSize = new CacheInfo(0, 1000000, 1),
                PageSize = 8 * 1024,
                FreeThreaded=true
            };
            //HashDatabase hashdb = HashDatabase.Open(String.Format("InputDB\\{0}.db", Path.GetFileNameWithoutExtension(name_)), hashconfig);
            return HashDatabase.Open(folder_name + String.Format("\\{0}.db", Path.GetFileNameWithoutExtension(name_)), hashconfig);//hashdb;
        }*/
    public static Database Get_BerkeleyDB(String db_folder,boolean writable,String db_name)
    {
        //boolean mkdir_ = new File("J:\\BerkeleyDB_Java").mkdir();
        System.out.println(db_folder);
        //FOR CREATING TIME ONLY
        
        boolean mkdir_ = new File(db_folder).mkdir();
        
        
        EnvironmentConfig envConf = new EnvironmentConfig();
        // environment will be created if not exists
       
        //FOR CREATING TIME ONLY
        envConf.setAllowCreate(writable);
        
        envConf.setCacheSize(1000000);
        //envConf.setCacheSize(10000);
        
        // open/create the DB environment using config
        //Environment dbEnv = new Environment(new File("J:\\BerkeleyDB_Java"), envConf);
        Environment dbEnv = new Environment(new File(db_folder), envConf);
        DatabaseConfig dbConf = new DatabaseConfig();
        // db will be created if not exits
        
        //FOR CREATING TIME ONLY
        dbConf.setAllowCreate(writable);
        dbConf.setSortedDuplicates(false);
        
        //DeferredWrite deals with late flushing of the db to hard drive and enables hasdb.sync otherwise flush will not work. 
        //FOR CREATING TIME ONLY
        dbConf.setDeferredWrite(true);
        return dbEnv.openDatabase(null, db_name, dbConf);
    }
    public static byte[] serialize(Object obj) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
    public static DatabaseEntry Get_BDB(String input)
    {
        DatabaseEntry entry=new DatabaseEntry();
        StringBinding.stringToEntry(input, entry);
        return entry;
    }
    public static DatabaseEntry Get_BDB_Dictionary(Map<Integer,Integer> input) throws IOException
    {     ByteArrayBinding binding=new  ByteArrayBinding();
          DatabaseEntry entry1=new DatabaseEntry();
          binding.objectToEntry(serialize(input),entry1);
          return entry1;
    }
}
