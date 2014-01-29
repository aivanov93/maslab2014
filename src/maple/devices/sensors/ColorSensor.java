package maple.devices.sensors;

import java.nio.ByteBuffer;

import maple.devices.Sensor;

public class ColorSensor extends Sensor {

	public int redValue;
	public int greenValue;
	
	@Override
	public byte getDeviceCode() {
		return 'L';
	}

	@Override
	public byte[] getInitializationBytes() {
		return new byte[] { };
	}

	@Override
	public void consumeMessageFromMaple(ByteBuffer buff) {
		int redMsb = buff.get();
		int redLsb = buff.get();
		int greenMsb = buff.get();
		int greenLsb = buff.get();
		redValue = (redMsb * 256) + ((int) redLsb & 0xff);
		greenValue = (greenMsb * 256) + ((int) greenLsb & 0xff);
	}

	@Override
	public int expectedNumBytesFromMaple() {
		return 4;
	}
	
	public int getRedValue() {
		return redValue;
	}
	
	public int getGreenValue() {
		return greenValue;
	}

}