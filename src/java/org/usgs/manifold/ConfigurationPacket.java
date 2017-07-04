package org.usgs.manifold;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

import org.usgs.ms.config.Configuration;
import org.usgs.ms.config.PropertiesReader;
import org.usgs.ms.config.ConfigStats;
import org.usgs.ms.config.converters.GeneralConverterSingle;



public class ConfigurationPacket extends Configuration {

    /** The default path to the configuration file. */
    public static final String DEFAULT_PATH = "/packet.properties";

    /** max number of stations imported into Manifold */
    public static final Integer MAX_STATIONS = 255;

     /** max number of channels imported into Manifold from each station*/
    public static final Integer MAX_CHANNELS = 8;

    /** The message type indicating single seismic packets. */
    public final short SEISMIC_MESSAGE_TYPE = 0;
    /** A {@code ChannelGroup} to hold earthworm. */
    public final ChannelGroup SEISMIC_EARTHWORM_CHANNELS;
    /** The interface that the earthworms will connect to on the server. */
    public final String SEISMIC_EARTHWORM_INTERFACE;
    /** The port that the earthworms will connect to on the server. */
    public final int SEISMIC_EARTHWORM_PORT;
    /** Time in between heartbeats sent to the connected worms. */
    public final int SEISMIC_EARTHWORM_HEARTBEAT_TIME;
    /** The heartbeat message to send. */
    public final String SEISMIC_EARTHWORM_HEARTBEAT_MESSAGE;

    /** The Installation number to send */
    public final int SEISMIC_EARTHWORM_INTALLATION;

    /** The Module number to send */
    public final int SEISMIC_EARTHWORM_MODULE;

    /** The message type indicating GPS packets. */
    public final short GPS_MESSAGE_TYPE = 4;
    /** The database to store GPS data in. */
    public final String GPS_DATABASE;
    /** The starting port number that the GPS data will be output on. */
   // private final int GPS_PORT_START;
    /** The ending port number that the GPS data will be output on */
   // private final int GPS_PORT_RANGE;
    /** Holds a mapping of station numbers to ports for GPS connections. */
    public final Map<Integer, Integer> GPS_STATION_TO_PORT_MAP;

    /** Holds a mapping of station numbers to the interface they are to use**/
    public final Map<Integer, String> GPS_STATION_TO_INTERFACE;

    /** Holds a mapping from station numbers to GPS station names */
    public final Map<Integer, String> GPS_STATION_NAME_MAP;

    /** Holds a mapping from station numbers to SCAN station names */
    public final Map<Integer, String> SCAN_STATION_NAME_MAP;
    
    /** The message type indicating one second scan packets. */
    public final short SCAN_MESSAGE_TYPE = 8;
    /** The database to store one second scan data in. */
    public final String SCAN_DATABASE;

    /** The message type indicating one second scan packets. */
    public final short RAIN_MESSAGE_TYPE = 10;
    /** The database to store one second scan data in. */
    public final String RAIN_DATABASE;

   /** The message type indicating multi message type */
    public final short MULTI_MESSAGE_TYPE = 12;

   /** Mapping from station numbers to station names */
   public final Map<Integer, String> MULTI_STATION_MAP;

   /** Mapping from station numbers to station names */
   public final Map<Integer, String> MULTI_TYPE_MAP;

   /** Mapping from station numbers and channels to station names */
   public final Map<Integer, String> MULTI_LOCATION_MAP;

   /** Mapping from station numbers and channels to an offset */
   public final Map<Integer, Integer> MULTI_OFFSET_MAP;

   /** Mapping from station numbers and channels to a sample rate lock */
   public final Map<Integer, Integer> MULTI_RATELOCK_MAP;


   public ConfigurationPacket() throws IOException {
        this(DEFAULT_PATH);
   }

    public ConfigurationPacket(String resourcePath) throws IOException {
        this.stats = new ConfigStats();
        PropertiesReader config = new PropertiesReader(stats, resourcePath);

        GeneralConverterSingle<String> stringConverter =
                new GeneralConverterSingle<String>(String.class);
        GeneralConverterSingle<Short> shortConverter =
                new GeneralConverterSingle<Short>(Short.class);
        GeneralConverterSingle<Integer> integerConverter =
                new GeneralConverterSingle<Integer>(Integer.class);

        // Load single seismic properties.
        SEISMIC_EARTHWORM_CHANNELS = new DefaultChannelGroup("earthWorm");
        SEISMIC_EARTHWORM_PORT = config.getValue("seismic.earthworm.port",
                PropertiesReader.REQUIRED, integerConverter);
        SEISMIC_EARTHWORM_INTERFACE = config.getValue("seismic.earthworm.interface",
                PropertiesReader.OPTIONAL, stringConverter);
        SEISMIC_EARTHWORM_HEARTBEAT_TIME =
                config.getValue("seismic.earthworm.heartbeat.time",
                PropertiesReader.REQUIRED, integerConverter);
        SEISMIC_EARTHWORM_HEARTBEAT_MESSAGE =
                config.getValue("seismic.earthworm.heartbeat.message",
                PropertiesReader.REQUIRED, stringConverter);
        SEISMIC_EARTHWORM_INTALLATION = config.getValue("seismic.earthworm.installation",
                PropertiesReader.REQUIRED, integerConverter);
        SEISMIC_EARTHWORM_MODULE = config.getValue("seismic.earthworm.module",
                PropertiesReader.REQUIRED, integerConverter);

        // Load GPS properties.
        GPS_DATABASE = config.getValue("gps.output.database",
                PropertiesReader.OPTIONAL, stringConverter);

        Map<Integer, String> gpsstationmap = new HashMap<Integer, String>();
        Map<Integer, Integer> portmap = new HashMap<Integer, Integer>();
        Map<Integer, String> interfacemap = new HashMap<Integer, String>();
        for(int i = 0; i < MAX_STATIONS; ++i) {
            String stationname = config.getValue("gps." + i + ".station" ,
                        PropertiesReader.OPTIONAL, stringConverter);
            Integer port = config.getValue("gps." + i + ".port",
                        PropertiesReader.OPTIONAL, integerConverter);
            String intface = config.getValue("gps." + i + ".interface" ,
                        PropertiesReader.OPTIONAL, stringConverter);
            gpsstationmap.put(i, stationname);

            if(port != null) {  // make shure that port is not null
                portmap.put(i, port);
            }
            
            if(intface != null) {  // make shure that the interface is not null
                interfacemap.put(i, intface);
            }
        }

        GPS_STATION_NAME_MAP = gpsstationmap;
        GPS_STATION_TO_PORT_MAP = portmap;
        GPS_STATION_TO_INTERFACE = interfacemap;

        // Load one second scan properties.
        SCAN_DATABASE = config.getValue("scan.output.database",
                PropertiesReader.OPTIONAL, stringConverter);
        
        // Load SCAN properties.
        Map<Integer, String> scanstationmap = new HashMap<Integer, String>();
        for(int i = 0; i < MAX_STATIONS; ++i) {
            String stationname = config.getValue("scan." + i + ".station" ,
                        PropertiesReader.OPTIONAL, stringConverter);
            scanstationmap.put(i, stationname);
        }
        
        SCAN_STATION_NAME_MAP = scanstationmap;

        RAIN_DATABASE = config.getValue("scan.output.database",
                PropertiesReader.OPTIONAL, stringConverter);

        

        Map<Integer, String> stationmap = new HashMap<Integer, String>();
        Map<Integer, String> typemap = new HashMap<Integer, String>();
        Map<Integer, String> locationmap = new HashMap<Integer, String>();
        Map<Integer, Integer> offsetmap = new HashMap<Integer, Integer>();
        Map<Integer, Integer> ratelockmap = new HashMap<Integer, Integer>();
        for (int i = 0; i < MAX_STATIONS; ++i) {
            for(int j = 0; j < MAX_CHANNELS; ++j) {
                String station = config.getValue("multi." + i + "."  + j + ".station",
                        PropertiesReader.OPTIONAL, stringConverter);
                String type = config.getValue("multi." + i + "."  + j + ".type",
                        PropertiesReader.OPTIONAL, stringConverter);
                String location = config.getValue("multi." + i + "."  + j + ".location",
                        PropertiesReader.OPTIONAL, stringConverter);
                Integer offset = config.getValue("multi." + i + "."  + j + ".offset",
                        PropertiesReader.OPTIONAL, integerConverter);
                Integer ratelock = config.getValue("multi." + i + "."  + j + ".sampleratelock",
                        PropertiesReader.OPTIONAL, integerConverter);
                stationmap.put((i * 10 + j), station);
                typemap.put((i * 10 + j),type);
                locationmap.put((i * 10 + j),location);
                offsetmap.put((i * 10 + j), offset);
                ratelockmap.put((i * 10 + j), ratelock);
            }
        }

        MULTI_STATION_MAP = stationmap;
        MULTI_TYPE_MAP = typemap;
        MULTI_LOCATION_MAP = locationmap;
        MULTI_OFFSET_MAP = offsetmap;
        MULTI_RATELOCK_MAP = ratelockmap;
    }
}
