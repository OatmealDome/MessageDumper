package me.oatmealdome.discord.MessageDumper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.UUID;

import com.google.common.util.concurrent.FutureCallback;

import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.entities.Channel;
import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.message.MessageHistory;
import de.btobastian.javacord.entities.permissions.PermissionState;
import de.btobastian.javacord.entities.permissions.PermissionType;
import de.btobastian.javacord.entities.permissions.Permissions;
import de.btobastian.javacord.entities.permissions.PermissionsBuilder;
import de.btobastian.javacord.entities.permissions.Role;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public class MessageListener implements MessageCreateListener {
	
	private final SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss.SSS");

	@Override
	public void onMessageCreate(DiscordAPI api, Message message) {
		String messageContents = message.getContent();
		if (!messageContents.startsWith("%"))
			return;
		
		if (!hasPermission(message.getAuthor(), message.getChannelReceiver().getServer())) {
			message.reply("You are not allowed to do that.");
			return;
		}
		
		String command = messageContents.substring(1, messageContents.length());
		switch (command) {
			case "dump": {
				try {
					getChannelMessages(message, message.getChannelReceiver());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case "help": {
				message.reply("Commands: %dump, %help");
				break;
			}
			default: {
				message.reply("Invalid command.");
				break;
			}
		}
	}
	
	private void getChannelMessages(Message commandMessage, Channel channel) {
		String tempFilePath = "/tmp/" + UUID.randomUUID().toString() + ".txt";
		File tempFile = new File(tempFilePath);
		
		try {
			tempFile.createNewFile();
		} catch (IOException ioe) {
			commandMessage.reply("Failed to create temporary file.");
			ioe.printStackTrace();
		}

		channel.getMessageHistory(Integer.MAX_VALUE, new FutureCallback<MessageHistory>() {
			@Override
			public void onSuccess(MessageHistory history) {
				try (PrintWriter writer = new PrintWriter(tempFile)) {
					int i = 0;
					for (Message message : history.getMessagesSorted()) {
						writer.println("[" + formatter.format(message.getCreationDate().getTime()) + "] " + message.getAuthor().getName() + ": " + message.getContent());
						i++;
					}
					
					writer.flush();
					
					commandMessage.replyFile(tempFile, "I dumped " + i + " messages from this channel.", new FutureCallback<Message>() {

						@Override
						public void onSuccess(Message arg0) {
							// do nothing
						}
						
						@Override
						public void onFailure(Throwable t) {
							commandMessage.reply("An internal error occurred.");
							t.printStackTrace();
						}
					
					});
					
				} catch (IOException ioe) {
					commandMessage.reply("An internal error occurred.");
					ioe.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(Throwable t) {
				commandMessage.reply("An internal error occurred.");
				t.printStackTrace();
			}
			
		});
	}
	
	private boolean hasPermission(User user, Server server) {
		Collection<Role> roles = user.getRoles(server);
		for (Role role : roles) {
			Permissions permissions = role.getPermissions();
			if (permissions.getState(PermissionType.READ_MESSAGE_HISTORY) == PermissionState.ALLOWED) {
				return true;
			}
		}
		return false;
	}

}
