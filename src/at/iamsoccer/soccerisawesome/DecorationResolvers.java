/*
 * This is the DecorationTag from Adventure
 *
 * This file is part of adventure, licensed under the MIT License.
 *
 * Copyright (c) 2017-2025 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package at.iamsoccer.soccerisawesome;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A transformation that applies any {@link TextDecoration}.
 *
 * @since 4.10.0
 */
public class DecorationResolvers {
    // vanilla decoration
    private static final String B = "b";
    private static final String I = "i";
    private static final String EM = "em";
    private static final String OBF = "obf";
    private static final String ST = "st";
    private static final String U = "u";

    private static final String REVERT = "!";

    public static final TagResolver OBFUSCATED = getTagResolver(TextDecoration.OBFUSCATED, OBF);
    public static final TagResolver ITALIC = getTagResolver(TextDecoration.ITALIC, EM, I);
    public static final TagResolver STRIKETHROUGH = getTagResolver(TextDecoration.STRIKETHROUGH, ST);
    public static final TagResolver UNDERLINED = getTagResolver(TextDecoration.UNDERLINED, U);
    public static final TagResolver BOLD = getTagResolver(TextDecoration.BOLD, B);

    private static @NotNull TagResolver getTagResolver(final TextDecoration decoration, final @Nullable String shortName, final @NotNull String @NotNull ... secondaryAliases) {
        return resolvers(decoration, shortName, secondaryAliases).getValue().reduce((l, r) -> TagResolver.builder().resolver(l).resolver(r).build()).get();
    }

    // create resolvers for canonical + configured alternates
    private static Map.Entry<TextDecoration, Stream<TagResolver>> resolvers(final TextDecoration decoration, final @Nullable String shortName, final @NotNull String @NotNull ... secondaryAliases) {
        final String canonicalName = TextDecoration.NAMES.key(decoration);
        final Set<String> names = new HashSet<>();
        names.add(canonicalName);
        if (shortName != null) names.add(shortName);
        Collections.addAll(names, secondaryAliases);

        return new AbstractMap.SimpleImmutableEntry<>(decoration, Stream.concat(
            Stream.of(SerializableResolver.claimingStyle(
                names,
                (args, ctx) -> create(decoration, args, ctx),
                claim(decoration, (state, emitter) -> emit(canonicalName, shortName == null ? canonicalName : shortName, state, emitter))
            )),
            names.stream().map(name -> TagResolver.resolver(REVERT + name, createNegated(decoration)))
        ));
    }

    private static final Map<TextDecoration, TagResolver> RESOLVERS = Stream.of(
            resolvers(TextDecoration.OBFUSCATED, OBF),
            resolvers(TextDecoration.BOLD, B),
            resolvers(TextDecoration.STRIKETHROUGH, ST),
            resolvers(TextDecoration.UNDERLINED, U),
            resolvers(TextDecoration.ITALIC, EM, I)
        )
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            ent -> ent.getValue().collect(TagResolver.toTagResolver()),
            (l, r) -> TagResolver.builder().resolver(l).resolver(r).build(),
            LinkedHashMap::new
        ));

    static final TagResolver RESOLVER = TagResolver.resolver(RESOLVERS.values());

    private DecorationResolvers() {
    }

    private static Tag create(final TextDecoration toApply, final ArgumentQueue args, final Context ctx) {
        final boolean flag = !args.hasNext() || !args.pop().isFalse();

        return Tag.styling(toApply.withState(flag));
    }

    private static Tag createNegated(final TextDecoration toApply) {
        return Tag.styling(toApply.withState(false));
    }

    private static @NotNull StyleClaim<TextDecoration.State> claim(final @NotNull TextDecoration decoration, final @NotNull BiConsumer<TextDecoration.State, TokenEmitter> emitable) {
        requireNonNull(decoration, "decoration");
        return StyleClaim.claim(
            "decoration_" + TextDecoration.NAMES.key(decoration),
            style -> style.decoration(decoration),
            state -> state != TextDecoration.State.NOT_SET,
            emitable
        );
    }

    private static void emit(final @NotNull String longName, final @NotNull String shortName, final TextDecoration.@NotNull State state, final @NotNull TokenEmitter emitter) {
        if (state == TextDecoration.State.FALSE) {
            emitter.tag(REVERT + longName);
        } else {
            emitter.tag(longName);
        }
    }

}
