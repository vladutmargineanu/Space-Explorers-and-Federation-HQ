import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.nio.charset.StandardCharsets;

/**
 * Class for a space explorer.
 */
public class SpaceExplorer extends Thread {
	private CommunicationChannel channel;
	private static Set<Integer> discovered;
	Integer hashCount;

    /**
	 * Creates a {@code SpaceExplorer} object.
	 * 
	 * @param hashCount
	 *            number of times that a space explorer repeats the hash operation
	 *            when decoding
	 * @param discovered
	 *            set containing the IDs of the discovered solar systems
	 * @param channel
	 *            communication channel between the space explorers and the
	 *            headquarters
	 */
	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.channel = channel;
		this.discovered = discovered;
		this.hashCount = hashCount;
	}

	public void run() {
		while(true) {

			Message message;
			int parentSolarSystem;
			int currentSolarSystem;
			String dataMessage;

			message = channel.getMessageHeadQuarterChannel();
			parentSolarSystem = message.getParentSolarSystem();
			currentSolarSystem = message.getCurrentSolarSystem();
			dataMessage = message.getData();

			if(HeadQuarter.EXIT.equals(dataMessage)) {
				return;
			}

			if(HeadQuarter.END.equals(dataMessage)) {
				continue;
			}

			if(discovered.contains(currentSolarSystem)){
				continue;
			}

			boolean add;
			if (!discovered.add(currentSolarSystem)) {
				add = false;
			} else {
				add = true;
			}

			String sendData;
			sendData = encryptMultipleTimes(dataMessage, hashCount);

			Message sendMessage;
			sendMessage = new Message(parentSolarSystem, currentSolarSystem, sendData);
			channel.putMessageSpaceExplorerChannel(sendMessage);
		}
	}

	/**
	 * Applies a hash function to a string for a given number of times (i.e.,
	 * decodes a frequency).
	 * 
	 * @param input
	 *            string to he hashed multiple times
	 * @param count
	 *            number of times that the string is hashed
	 * @return hashed string (i.e., decoded frequency)
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;
		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Applies a hash function to a string (to be used multiple times when decoding
	 * a frequency).
	 * 
	 * @param input
	 *            string to be hashed
	 * @return hashed string
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			// convert to string
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}