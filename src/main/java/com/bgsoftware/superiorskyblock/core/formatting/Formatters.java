package com.bgsoftware.superiorskyblock.core.formatting;

import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.formatting.impl.BlockPositionFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.BooleanFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.BorderColorFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.CapitalizedFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.ChatFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.ColorFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.CommaFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.DateFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.FancyNumberFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.LocaleFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.LocationFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.NumberFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.RatingFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.StripColorFormatter;
import com.bgsoftware.superiorskyblock.core.formatting.impl.TimeFormatter;
import org.bukkit.Location;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Formatters {

    public static final IBiFormatter<Boolean, Locale> BOOLEAN_FORMATTER = BooleanFormatter.getInstance();
    public static final IBiFormatter<BorderColor, Locale> BORDER_COLOR_FORMATTER = BorderColorFormatter.getInstance();
    public static final IFormatter<String> CAPITALIZED_FORMATTER = CapitalizedFormatter.getInstance();
    public static final IFormatter<String> COLOR_FORMATTER = ColorFormatter.getInstance();
    public static final IFormatter<Stream<String>> COMMA_FORMATTER = CommaFormatter.getInstance();
    public static final IFormatter<Date> DATE_FORMATTER = DateFormatter.getInstance();
    public static final IBiFormatter<Number, Locale> FANCY_NUMBER_FORMATTER = FancyNumberFormatter.getInstance();
    public static final IFormatter<Locale> LOCALE_FORMATTER = LocaleFormatter.getInstance();
    public static final IFormatter<Location> LOCATION_FORMATTER = LocationFormatter.getInstance();
    public static final IFormatter<Number> NUMBER_FORMATTER = NumberFormatter.getInstance();
    public static final IBiFormatter<Number, Locale> RATING_FORMATTER = RatingFormatter.getInstance();
    public static final IFormatter<String> STRIP_COLOR_FORMATTER = StripColorFormatter.getInstance();
    public static final IBiFormatter<Duration, Locale> TIME_FORMATTER = TimeFormatter.getInstance();
    public static final IFormatter<ChatFormatter.ChatFormatArgs> CHAT_FORMATTER = ChatFormatter.getInstance();
    public static final IBiFormatter<BlockPosition, WorldInfo> BLOCK_POSITION_FORMATTER = BlockPositionFormatter.getInstance();

    private Formatters() {

    }

    public static <T> List<String> formatList(List<T> list, IFormatter<T> formatter) {
        return list.stream().map(formatter::format).collect(Collectors.toList());
    }

}
