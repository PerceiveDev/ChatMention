package com.perceivedev.chatmention;

import static com.perceivedev.perceivecore.reflection.ReflectionUtil.NameSpace.NMS;
import static com.perceivedev.perceivecore.reflection.ReflectionUtil.NameSpace.OBC;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.perceivedev.perceivecore.PerceiveCore;
import com.perceivedev.perceivecore.packet.Packet;
import com.perceivedev.perceivecore.reflection.ReflectionUtil;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.MemberPredicate;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.MethodPredicate;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.ReflectResponse;
import com.perceivedev.perceivecore.reflection.ReflectionUtil.ReflectResponse.ResultType;

import net.minecraft.server.v1_10_R1.PacketPlayOutChat;

/**
 * Extracts text from a PacketPlayOutChat
 */
public class ChatPacketTextUtils {

    private static final Class<?> PACKET_PLAY_OUT_CHAT;

    static {
        Optional<Class<?>> target = ReflectionUtil.getClass("{nms}.PacketPlayOutChat");
        if (!target.isPresent()) {
            ChatMention.getPlugin(ChatMention.class).getLogger()
                      .severe("Could not find chat packet class! The plugin will now be disabled.");
            Bukkit.getPluginManager().disablePlugin(ChatMention.getPlugin(ChatMention.class));
            PACKET_PLAY_OUT_CHAT = null;
        } else {
            PACKET_PLAY_OUT_CHAT = target.get();
        }
    }

    private static Class<?> CRAFT_CHAT_MESSAGE;
    private static Method   craftChatFromString;

    private static Class<?>                 CHAT_MODIFIER_CLASS;
    private static Class<?>                 CHAT_COMPONENT_TEXT_CLASS;
    private static Class<?>                 I_CHAT_BASE_COMPONENT_CLASS;
    private static Function<Object, String> formatTranslator;
    private static Function<Object, String> colorTranslator;
    private static boolean error = false;

    static {
        {
            Optional<Class<?>> chatModifierOptional = ReflectionUtil.getClass(NMS, "ChatModifier");
            if (!chatModifierOptional.isPresent()) {
                PerceiveCore.getInstance().getLogger().warning("Can't find ChatModifier class");
                error = true;
            } else {
                CHAT_MODIFIER_CLASS = chatModifierOptional.get();
            }
        }
        {
            Optional<Class<?>> chatComponentTextOptional = ReflectionUtil.getClass(NMS, "ChatComponentText");
            if (!chatComponentTextOptional.isPresent()) {
                PerceiveCore.getInstance().getLogger().warning("Can't find ChatComponentText class");
                error = true;
            } else {
                CHAT_COMPONENT_TEXT_CLASS = chatComponentTextOptional.get();
            }
        }
        {
            Optional<Class<?>> iChatBaseOptional = ReflectionUtil.getClass(NMS, "IChatBaseComponent");
            if (!iChatBaseOptional.isPresent()) {
                PerceiveCore.getInstance().getLogger().warning("Can't find IChatBaseComponent class");
                error = true;
            } else {
                I_CHAT_BASE_COMPONENT_CLASS = iChatBaseOptional.get();
            }
        }
        {
            Optional<Class<?>> craftChatMessageOpt = ReflectionUtil.getClass(OBC, "util.CraftChatMessage");
            if (!craftChatMessageOpt.isPresent()) {
                PerceiveCore.getInstance().getLogger().warning("Can't find CraftChatMessage class");
                error = true;
            } else {
                CRAFT_CHAT_MESSAGE = craftChatMessageOpt.get();
            }
        }
        {
            if (!error) {
                ReflectResponse<Method> craftChatMessageOpt = ReflectionUtil.getMethod(CRAFT_CHAT_MESSAGE,
                          new MethodPredicate()
                                    .withName("fromString")
                                    .withParameters(String.class));
                if (!craftChatMessageOpt.isValuePresent()) {
                    PerceiveCore.getInstance().getLogger().warning("Can't find CraftChatMessage#fromString method");
                    error = true;
                } else {
                    craftChatFromString = craftChatMessageOpt.getValue();
                }
            }
        }

        if (!error) {
            formatTranslator = o -> {
                if (!CHAT_MODIFIER_CLASS.isAssignableFrom(o.getClass())) {
                    return null;
                }
                StringBuilder formatResult = new StringBuilder();

                ReflectionUtil.invokeInstanceMethod(o, "isStrikethrough", new Class[0]).get().ifPresent(o1 -> {
                    if ((Boolean) o1) {
                        formatResult.append(ChatColor.STRIKETHROUGH.toString());
                    }
                });
                ReflectionUtil.invokeInstanceMethod(o, "isBold", new Class[0]).get().ifPresent(o1 -> {
                    if ((Boolean) o1) {
                        formatResult.append(ChatColor.BOLD.toString());
                    }
                });
                ReflectionUtil.invokeInstanceMethod(o, "isItalic", new Class[0]).get().ifPresent(o1 -> {
                    if ((Boolean) o1) {
                        formatResult.append(ChatColor.ITALIC.toString());
                    }
                });
                ReflectionUtil.invokeInstanceMethod(o, "isUnderlined", new Class[0]).get().ifPresent(o1 -> {
                    if ((Boolean) o1) {
                        formatResult.append(ChatColor.UNDERLINE.toString());
                    }
                });
                ReflectionUtil.invokeInstanceMethod(o, "isRandom", new Class[0]).get().ifPresent(o1 -> {
                    if ((Boolean) o1) {
                        formatResult.append(ChatColor.MAGIC.toString());
                    }
                });

                return formatResult.toString();
            };

            colorTranslator = o -> {
                if (!CHAT_MODIFIER_CLASS.isAssignableFrom(o.getClass())) {
                    return null;
                }
                StringBuilder colorResult = new StringBuilder();

                ReflectionUtil.invokeInstanceMethod(o, "getColor", new Class[0]).get().ifPresent(o1 ->
                          colorResult.append(o1.toString())
                );

                return colorResult.toString();
            };
        }
    }

    /**
     * Decodes the String from a packet
     *
     * @param packet The Packet to decode
     *
     * @return The decoded packet or null, if an error occurred.
     */
    public static String decode(Packet packet) {
        if (packet.getNMSPacket().getClass() != PACKET_PLAY_OUT_CHAT) {
            throw new IllegalArgumentException("Packet not of type PacketPlayOutChat");
        }

        ReflectResponse<Object> reflectResponse = ReflectionUtil.getFieldValue(PacketPlayOutChat.class,
                  packet.getNMSPacket(),
                  new MemberPredicate<Field>().withName("a"));

        if (!reflectResponse.isValuePresent()) {
            return null;
        }

        // is not a chat component
        if (!I_CHAT_BASE_COMPONENT_CLASS.isAssignableFrom(reflectResponse.getValue().getClass())) {
            ChatMention.getPlugin(ChatMention.class).getLogger()
                      .log(Level.SEVERE, "No chat component with name 'a' found!");
            return null;
        }

        @SuppressWarnings("unchecked")
        Iterable<Object> message = (Iterable<Object>) reflectResponse.getValue();

        StringBuilder builder = new StringBuilder();

        boolean wasFormattedBefore = false;
        for (Object component : message) {
            if (component == null) {
                continue;
            }
            if (!CHAT_COMPONENT_TEXT_CLASS.isAssignableFrom(component.getClass())) {
                continue;
            }

            String text;
            {
                ReflectResponse<Object> textResponse = ReflectionUtil.invokeInstanceMethod(component,
                          "getText",
                          new Class[0]);
                if (!textResponse.isValuePresent()) {
                    if (textResponse.getResultType() == ResultType.ERROR) {
                        ChatMention.getPlugin(ChatMention.class).getLogger()
                                  .log(Level.WARNING, "Error getting text", textResponse.getException());
                    }
                    continue;
                } else {
                    text = (String) textResponse.getValue();
                }
            }
            Object modifier;
            {
                ReflectResponse<Object> modifierResponse = ReflectionUtil.invokeInstanceMethod(component,
                          "getChatModifier",
                          new Class[0]);
                if (!modifierResponse.isSuccessful()) {
                    if (modifierResponse.getResultType() == ResultType.ERROR) {
                        ChatMention.getPlugin(ChatMention.class).getLogger().
                                  log(Level.WARNING, "Error getting modifier", modifierResponse.getException());
                    } else {
                        ChatMention.getPlugin(ChatMention.class).getLogger()
                                  .log(Level.WARNING, "Error getting modifier: " + modifierResponse.getResultType());
                    }
                    continue;
                } else {
                    modifier = modifierResponse.getValue();
                }
            }

            if (modifier != null) {
                //noinspection StringConcatenationInLoop
                text = formatTranslator.apply(modifier) + text;
                //noinspection StringConcatenationInLoop
                text = colorTranslator.apply(modifier) + text;
            }

            if (!isFormatted(modifier)) {
                if (wasFormattedBefore) {
                    text = ChatColor.RESET + text;
                }
            }
            wasFormattedBefore = isFormatted(modifier);

            builder.append(text);
        }

        return builder.toString();
    }

    /**
     * Checks if the modifier is formatted
     *
     * @param modifier The modifier to check
     *
     * @return True if the modifier is not null and formatted
     */
    private static boolean isFormatted(Object modifier) {
        if (modifier == null) {
            return false;
        }
        AtomicBoolean wasFormatted = new AtomicBoolean(false);
        ReflectionUtil.invokeInstanceMethod(modifier, "isStrikethrough", new Class[0]).get().ifPresent(o1 -> {
            if ((Boolean) o1) {
                wasFormatted.set(true);
            }
        });
        ReflectionUtil.invokeInstanceMethod(modifier, "isBold", new Class[0]).get().ifPresent(o1 -> {
            if ((Boolean) o1) {
                wasFormatted.set(true);
            }
        });
        ReflectionUtil.invokeInstanceMethod(modifier, "isItalic", new Class[0]).get().ifPresent(o1 -> {
            if ((Boolean) o1) {
                wasFormatted.set(true);
            }
        });
        ReflectionUtil.invokeInstanceMethod(modifier, "isUnderlined", new Class[0]).get().ifPresent(o1 -> {
            if ((Boolean) o1) {
                wasFormatted.set(true);
            }
        });
        ReflectionUtil.invokeInstanceMethod(modifier, "isRandom", new Class[0]).get().ifPresent(o1 -> {
            if ((Boolean) o1) {
                wasFormatted.set(true);
            }
        });

        ReflectionUtil.invokeInstanceMethod(modifier, "getColor", new Class[0]).get().ifPresent(o1 -> wasFormatted.set(true));

        return wasFormatted.get();
    }

    /**
     * Encodes the message in an IChatBaseComponent
     *
     * @param message The message to encode
     *
     * @return The IChatBaseComponent for it
     */
    public static Object encode(String message) {
        ReflectResponse<Object> reflectResponse = ReflectionUtil.invokeMethod(craftChatFromString, null, message);
        if (!reflectResponse.isValuePresent()) {
            if (reflectResponse.getResultType() == ResultType.ERROR) {
                ChatMention.getPlugin(ChatMention.class).getLogger().
                          log(Level.WARNING, "Error creating chat message", reflectResponse.getException());
            } else {
                ChatMention.getPlugin(ChatMention.class).getLogger()
                          .log(Level.WARNING, "Error creating chat message: " + reflectResponse.getResultType());
            }
            return null;
        }
        return ((Object[]) reflectResponse.getValue())[0];
    }
}
