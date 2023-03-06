package utils;

public class Azure {

	public static final String VOICE_NAME = "en-IN-NeerjaNeural";
	public static final int SPEECH_RATE = 14;
	public static final int PITCH = 1;

	public static String wrapWithSSML(String text) {
		String prebody = "<prosody rate=\"" + SPEECH_RATE + "%\" pitch=\"" + PITCH + "%\"> " + text + " </prosody>";
		String voice = "<voice name=\"" + VOICE_NAME + "\"> " + prebody + " </voice>";
		String speak = "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"en-US\"> " + voice + " </speak>";
		if (speak.contains("~HANGUP")) {
			speak = speak.replace("~HANGUP", "");
			speak = speak + " ~HANGUP";
		}
		return speak;
	}
	private Azure() {}
}
