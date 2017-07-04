package org.usgs.manifold.packet.data;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.usgs.manifold.ConfigurationPacket;
import org.usgs.manifold.Initialize;

import org.usgs.manifold.packet.earthworm.TraceBufferPacket;
import org.usgs.manifold.packet.earthworm.TraceBuffer2Packet;

/**
 *
 */
public class SendMulti {

private static ConfigurationPacket config = Initialize.getPacketConfig();

    // Prevent instantiation
    private SendMulti() {
    }

    /**
     * Write out the DataSingleSeismic to all connected earthworms.
     *
     * @param seismicData the packet to be sent out.
     * @throws IllegalStateException if the required variables have not been
     *         channelSetup.
     */
    public static synchronized void sendToEarthworm(
            DataMulti seismicData) {

        //check for nulls
        if(config.MULTI_STATION_MAP.get((seismicData.getStationNumber() * 10 + seismicData.getChannel()) ) == null ||
        config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel() ) == null ||
        config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel() ) == null ) {
            System.out.println("no translation for " + ((Integer)seismicData.getStationNumber() + "." + seismicData.getChannel()) );
            return;
        }
        
        
        //the off set for all of the data
        Integer dataoffset = 0;
        
        //set the offset if there is one
        if(config.MULTI_OFFSET_MAP.get((seismicData.getStationNumber() * 10 + seismicData.getChannel()) ) != null) {
        	dataoffset = config.MULTI_OFFSET_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel());
        }
        
        //use a sample rate lock if one was provied.
        if(config.MULTI_RATELOCK_MAP.get((seismicData.getStationNumber() * 10 + seismicData.getChannel()) ) != null) {
            
                int ratelock = config.MULTI_RATELOCK_MAP.get((seismicData.getStationNumber() * 10 + seismicData.getChannel()) );
                
                // apple the rate lock transformation if there is a missing sample(samplerate -1) or has an extra sample(samplerate +1)
                // if there is more then one extra samples drop the packet
                
                // 2 or more samples in packet
                if(seismicData.getNumberOfSamples() > ratelock + 1){
                    System.err.println("Too many samples in packet from C16 " + seismicData.getNumberOfSamples() + 
                            " samples received, " + ratelock + " samples expected");
                    return;                    
                }            
                
                // 2 or less samples missing form packet
                if(seismicData.getNumberOfSamples() < ratelock - 1){
                    System.err.println("Too few samples in packet from C16 " + seismicData.getNumberOfSamples() + 
                            " samples received, " + ratelock + " samples expected");
                    return;                    
                }
                                
                // extra sample in packett
                if(seismicData.getNumberOfSamples() == ratelock + 1){
                    System.err.println("one too many samples in packet droping sample");
                    seismicData.dropLastSample();
                }
                
                // missing sample in packett
                if(seismicData.getNumberOfSamples() == ratelock - 1){
                    System.err.println("one too few samples in packet droping sample");
                    seismicData.addExtraSample();
                }
                
        }
        

        //switched to use a tracebuffer2
        // Create trace buffer data to send out.
        //byte[] traceBufferData = new TraceBufferPacket(
        //        config.MULTI_STATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
        //        seismicData.getNetworkID(),
        //        config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
        //        config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
        //        seismicData.getStartTime(),
        //        seismicData.getEndTime(),
        //        seismicData.getSampleRate(),
        //        seismicData.getData()).getMessage();

         byte[] traceBufferData = new TraceBuffer2Packet(
                config.MULTI_STATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
                seismicData.getNetworkID(),
                config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
                config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel()),
                seismicData.getStartTime(),
                seismicData.getEndTime(),
                seismicData.getSampleRate(),
                seismicData.getDataWithOffset(dataoffset)).getMessage();

        // Wrap the byte[] with a ChannelBuffer so it can be sent to a channel.
        ChannelBuffer output =
                ChannelBuffers.wrappedBuffer(traceBufferData);

        config.SEISMIC_EARTHWORM_CHANNELS.write(output);

        //TODO: Remove packet data print to std out
        System.out.println("Seismic: time =" + seismicData.getStartTime()
            + ", station = " + config.MULTI_STATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel())
            + ", type = " + config.MULTI_TYPE_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel())
            + ", location = " + config.MULTI_LOCATION_MAP.get(seismicData.getStationNumber() * 10 + seismicData.getChannel())
            //+ "samples = "+ seismicData.getData().length);
            );    
    }
}
