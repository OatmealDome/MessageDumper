package me.oatmealdome.discord.MessageDumper;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;

public class MessageDumper {
	
	private boolean isStarted = false;
	private DiscordAPI discordApi;
	
	public MessageDumper() {
		
	}
	
	public void start(String token) {
		if (isStarted)
			return;
		
		discordApi = Javacord.getApi(token, true);	
		
		discordApi.connect(new FutureCallback<DiscordAPI>() {
			public void onSuccess(final DiscordAPI api) {
				discordApi.registerListener(new MessageListener());
			}
			
			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});
		
	}
	
	public static void main(String[] args) {
		new MessageDumper().start(args[0]);
	}
	
}
