package at.iamsoccer.soccerisawesome.itemrename.dialog.templates.generic.interfaces;

import at.hugob.plugin.library.config.MiniMsgLegacyHybridSerializer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.ApiStatus;

import java.util.stream.Stream;

public interface IComponentParser<User extends Audience> {
    default Component parse(String string, TagResolver resolver) {
        return MiniMsgLegacyHybridSerializer.INSTANCE.deserialize(string, resolver);
    }

    @ApiStatus.Internal
    default Component parse(User user, String string) {
        return parse(string, tagResolver(user));
    }

    @ApiStatus.Internal
    default Stream<Component> parse(User user, Stream<String> strings) {
        var resolver = tagResolver(user);
        return strings.map(string -> parse(string, resolver));
    }
    /**
     * The generic tag resolver to use for any String this dialog may parse into a component
     *
     * @return the resolver to parse additional tags that may be needed
     */
    default TagResolver tagResolver(User user) {
        return TagResolver.empty();
    }
}
