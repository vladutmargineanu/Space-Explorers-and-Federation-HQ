import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
    private Map<Long, Message> parentNodeThreads;
    private boolean currentThread;
    private long getThreadId;
    private LinkedBlockingQueue<Message> messagesSpaceExplorers;
	private LinkedBlockingQueue<Message> messagesHeadQuarters;

	public CommunicationChannel() {
        messagesSpaceExplorers = new LinkedBlockingQueue<>();
        messagesHeadQuarters = new LinkedBlockingQueue<>();
        parentNodeThreads = new HashMap<Long, Message>();
        currentThread = false;
        getThreadId = -100;
    }

	/**
	 * Puts a message on the space explorer channel (i.e., where space explorers write to and 
	 * headquarters read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageSpaceExplorerChannel(Message message) {
		try {
			messagesSpaceExplorers.put(message);
		}
		catch (InterruptedException e) {
			return;
		}
	}

	/**
	 * Gets a message from the space explorer channel (i.e., where space explorers write to and
	 * headquarters read from).
	 * 
	 * @return message from the space explorer channel
	 */
	public Message getMessageSpaceExplorerChannel() {

        try {
            Message message = messagesSpaceExplorers.take();
            return message;
		}
		catch (InterruptedException e)
		{
			return null;
		}
    }

	/**
	 * Puts a message on the headquarters channel (i.e., where headquarters write to and 
	 * space explorers read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageHeadQuarterChannel(Message message) {

		String dataMessage;
        int currentSolarSystem;
        long currentThreadId;
        Message putMessage;

        dataMessage = message.getData();
        currentSolarSystem = message.getCurrentSolarSystem();
        currentThreadId = Thread.currentThread().getId();
        putMessage = new Message(-1, -1, null);


        if (HeadQuarter.END.equals(dataMessage) || HeadQuarter.EXIT.equals(dataMessage))
		{
            if (!(currentThread || !parentNodeThreads.isEmpty())) {
                getThreadId = currentThreadId;
                currentThread = true;
            }

            if (getThreadId == currentThreadId) {
                putMessage = message;
            } else {
                return;
            }

            try {
                messagesHeadQuarters.put(putMessage);
            } catch (InterruptedException e) {
                return;
            }
        }
		else {

            if (!parentNodeThreads.containsKey(currentThreadId)) {
                if (Objects.equals(dataMessage, "NO_PARENT")) {
                    currentSolarSystem = -1;
                }
                putMessage.setParentSolarSystem(currentSolarSystem);
                putMessage.setCurrentSolarSystem(-1);
                putMessage.setData(null);

                AtomicReference<Message> message1;
                message1 = new AtomicReference<>(parentNodeThreads.put(currentThreadId, putMessage));
                return;
            }

            if (parentNodeThreads.containsKey(currentThreadId)) {
                putMessage = parentNodeThreads.remove(currentThreadId);
                putMessage.setCurrentSolarSystem(currentSolarSystem);
                putMessage.setData(dataMessage);
            }

            try {
                messagesHeadQuarters.put(putMessage);
            } catch (InterruptedException e) {
                return;
            }
        }
	}

	/**
	 * Gets a message from the headquarters channel (i.e., where headquarters write to and
	 * space explorer read from).
	 * 
	 * @return message from the header quarter channel
	 */
	public Message getMessageHeadQuarterChannel() {

        try {
            AtomicReference<Message> message;
            message = new AtomicReference<>(messagesHeadQuarters.take());
            Message message1 = message.get();
            return message1;
		}
		catch (InterruptedException e)
		{
			return null;
		}

    }
}
