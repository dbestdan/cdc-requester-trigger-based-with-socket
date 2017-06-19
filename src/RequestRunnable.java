import java.util.HashMap;
import java.util.Map;

public class RequestRunnable implements Runnable {
	private long requestInterval = 0l;
	private long timeWindow = 0L;
	private Map<Long, Long> pc4RequestList = null;
	private Map<Long, Long> pc5RequestList = null;
	private boolean checker = true;

	public RequestRunnable(Map<Long, Long> pc4RequestList, Map<Long, Long> pc5RequestList) {
		requestInterval = Long.parseLong(System.getProperty("requestInterval"));
		timeWindow = Long.parseLong(System.getProperty("timeWindow"));
		this.pc4RequestList =pc4RequestList;
		this.pc5RequestList = pc5RequestList;
	}

	@Override
	public void run() {
		// TODO This will continuously fire request with current time stamp and
		// window time
		while (true) {
			try {
				Thread.sleep(requestInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Long requestStartTime = System.currentTimeMillis();
			Long requestExpirationTime = requestStartTime + timeWindow;

			if (checker) {
				synchronized (pc4RequestList) {
					pc4RequestList.put(requestStartTime, requestExpirationTime);
				}
			}else {
				synchronized (pc5RequestList) {
					pc5RequestList.put(requestStartTime, requestExpirationTime);
				}
			}
			
			checker = checker?false:true;

		}

	}

}
