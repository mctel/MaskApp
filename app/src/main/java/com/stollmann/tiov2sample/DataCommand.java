package com.stollmann.tiov2sample;

import java.nio.ByteBuffer;

public class DataCommand{

	public short[] Channel1 = new short[4];
	public short[] Channel2 = new short[4];
	public int CTR; //control


    public DataCommand(byte[] byteArray){

		Channel1[0] = ByteBuffer.wrap(byteArray,2,2).getShort(); //byteArray,offset,length
       	Channel2[0] = ByteBuffer.wrap(byteArray,4,2).getShort();
	   	Channel1[1] = ByteBuffer.wrap(byteArray,6,2).getShort();
	   	Channel2[1] = ByteBuffer.wrap(byteArray,8,2).getShort();
		Channel1[2] = ByteBuffer.wrap(byteArray,10,2).getShort();
        Channel2[2] = ByteBuffer.wrap(byteArray,12,2).getShort();
        Channel1[3] = ByteBuffer.wrap(byteArray,14,2).getShort();
        Channel2[3] = ByteBuffer.wrap(byteArray,16,2).getShort();
		CTR = byteArray[19];

    }

	@Override
	public String toString() {
		return String.format("Ch1VAL1: %d\nCh2VAL1: %d\nCh1VAL2: %d\nCh2VAL2: %d\nCh1VAL3: %d\n" +
						"Ch3VAL3: %d\nCh1VAL4: %d\nCh2VAL4: %d\nCTR: %d\n", Channel1[0],Channel2[0],
				Channel1[1],Channel2[1],Channel1[2],Channel2[2],Channel1[3],Channel2[3],CTR);
	}
}
