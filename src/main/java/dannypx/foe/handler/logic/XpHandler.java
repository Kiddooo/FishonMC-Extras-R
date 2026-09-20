package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XpHandler extends Handler {

    public enum XpType {
        PLAYER,
        LOCATION,
        CREW,
        PET
    }

    private static final XpHandler INSTANCE = new XpHandler();

    public static XpHandler instance() {
        return INSTANCE;
    }

    private static final Pattern XP_PATTERN = Pattern.compile(
            "^\\+([\\d,]+(?:\\.\\d+)?)([KMB]?)\\s*(LXP|XP)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final int PLAYER_COLOUR = 0x55FFFF;
    private static final int LOCATION_COLOUR = 0xAAF682;
    private static final int CREW_COLOUR = 0x00AA00;
    private static final int PET_COLOUR = 0xFF55FF;

    private final Map<XpType, Long> lastAward =
            new EnumMap<>(XpType.class);

    private XpHandler() {
        init();
    }

    @Override
    public void init() {
        for (XpType type : XpType.values()) {
            lastAward.put(type, 0L);
        }
    }

    public void onOverlay(Component message) {
        if (message == null
                || !message.getString()
                .toLowerCase(Locale.ROOT)
                .contains("xp")) {
            return;
        }

        Map<XpType, Long> awards =
                new EnumMap<>(XpType.class);

        StringBuilder fragmentText = new StringBuilder();
        int[] previousColour = {-1};

        // Visit the component's text with its effective formatting.
        // Combine consecutive fragments of the same colour in case
        // an XP award is split across multiple components.
        message.visit((style, fragment) -> {
            int colour = style.getColor() == null
                    ? -1
                    : style.getColor().getValue();

            if (colour != previousColour[0]) {
                parseAward(
                        fragmentText.toString(),
                        previousColour[0],
                        awards
                );

                fragmentText.setLength(0);
                previousColour[0] = colour;
            }

            fragmentText.append(fragment);

            return Optional.empty();
        }, Style.EMPTY);

        // Process the final accumulated fragment.
        parseAward(
                fragmentText.toString(),
                previousColour[0],
                awards
        );

        if (awards.isEmpty()) {
            return;
        }

        // Latest awards refer to THIS notification only.
        // A type absent from the notification gets a value of zero.
        for (XpType type : XpType.values()) {
            lastAward.put(
                    type,
                    awards.getOrDefault(type, 0L)
            );
        }

        LoggerHandler.info(
                "[FOER XP] player=" + getLastPlayer()
                        + " location=" + getLastLocation()
                        + " crew=" + getLastCrew()
                        + " pet=" + getLastPet()
        );

        LocationXpHandler.instance()
                .onLocationXpAward(getLastLocation());

        // Let FOER's existing event/trigger system decide what
        // to do with the award, including updating custom trackers.
        EventHandler.instance().onXpGain();
    }

    private static void parseAward(
            String text,
            int colour,
            Map<XpType, Long> awards
    ) {
        Matcher matcher = XP_PATTERN.matcher(text.trim());

        if (!matcher.matches()) {
            return;
        }

        String unit = matcher.group(3)
                .toUpperCase(Locale.ROOT);

        XpType type;

        if ("LXP".equals(unit)) {
            if (colour != LOCATION_COLOUR) {
                return;
            }

            type = XpType.LOCATION;
        } else {
            type = switch (colour) {
                case PLAYER_COLOUR -> XpType.PLAYER;
                case CREW_COLOUR -> XpType.CREW;
                case PET_COLOUR -> XpType.PET;
                default -> null;
            };
        }

        if (type == null || awards.containsKey(type)) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(
                    matcher.group(1).replace(",", "")
            );

            long multiplier = switch (
                    matcher.group(2).toUpperCase(Locale.ROOT)
                    ) {
                case "K" -> 1_000L;
                case "M" -> 1_000_000L;
                case "B" -> 1_000_000_000L;
                default -> 1L;
            };

            long value = amount
                    .multiply(BigDecimal.valueOf(multiplier))
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();

            awards.put(type, value);

        } catch (NumberFormatException | ArithmeticException ignored) {
            // Ignore malformed or excessively large XP values.
        }
    }

    public long getLastPlayer() {
        return lastAward.getOrDefault(XpType.PLAYER, 0L);
    }

    public long getLastLocation() {
        return lastAward.getOrDefault(XpType.LOCATION, 0L);
    }

    public long getLastCrew() {
        return lastAward.getOrDefault(XpType.CREW, 0L);
    }

    public long getLastPet() {
        return lastAward.getOrDefault(XpType.PET, 0L);
    }

    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>>
    _getFields() {
        return Map.of();
    }
}