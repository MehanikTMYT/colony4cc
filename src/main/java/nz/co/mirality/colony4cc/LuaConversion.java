package nz.co.mirality.colony4cc;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class LuaConversion {
    public interface IConverter {
        Object convert(Object value);
    }

    private LuaConversion() {}

    private static final List<IConverter> CONVERTERS = new ArrayList<>();

    public static void register(final IConverter converter) { CONVERTERS.add(converter); }

    static {
        register(new CollectionConverter());
        register(new MinecraftConverter());
    }

    public static Object convert(Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }

        for (IConverter converter : CONVERTERS) {
            Object result = converter.convert(value);
            if (result != null) return result;
        }

        Colony4CC.LOGGER.error("Unable to convert value type {}", value.getClass().getName());
        return null;
    }

    private static class CollectionConverter implements IConverter {
        @Override
        public Object convert(Object value) {
            if (value instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) value;
                Map<Object, Object> result = new HashMap<>();
                map.forEach((k, v) -> result.put(String.valueOf(k), LuaConversion.convert(v)));
                return result;
            }
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                Map<Object, Object> result = new HashMap<>();
                for (int i = 0, count = list.size(); i < count; ++i) {
                    result.put(i + 1, LuaConversion.convert(list.get(i)));
                }
                return result;
            }
            if (value instanceof Collection) {
                Collection<?> list = (Collection<?>) value;
                Map<Object, Object> result = new HashMap<>();
                final int[] count = {0};
                list.forEach(v -> result.put(++count[0], LuaConversion.convert(v)));
                return result;
            }
            return null;
        }
    }

    public static class MinecraftConverter implements IConverter {
        @Override
        public Object convert(Object value) {
            if (value instanceof ItemStack) {
                return convertStack((ItemStack) value);
            }
            if (value instanceof GlobalPos) {
                return convert((GlobalPos) value);
            }
            if (value instanceof BlockPos) {
                return convert((BlockPos) value);
            }
            if (value instanceof CompoundTag) {
                return convertNbt((CompoundTag) value);
            }
            if (value instanceof Identifier) {
                return ((Identifier) value).toString();
            }
            if (value instanceof Component) {
                return ((Component) value).getString();
            }
            return null;
        }

        @Nonnull
        private Map<Object, Object> convert(GlobalPos pos) {
            Map<Object, Object> result = convert(pos.pos());
            result.put("world", pos.dimension().identifier().toString());
            return result;
        }

        @Nonnull
        private Map<Object, Object> convert(BlockPos pos) {
            Map<Object, Object> result = new HashMap<>();
            result.put("x", pos.getX());
            result.put("y", pos.getY());
            result.put("z", pos.getZ());
            return result;
        }

        @Nonnull
        public static Map<Object, Object> convertStack(@Nonnull ItemStack stack) {
            Map<Object, Object> result = new HashMap<>();

            boolean wasEmptyish = stack != ItemStack.EMPTY && stack.getCount() == 0;
            if (wasEmptyish) {
                // ordinarily zero-count stacks are treated as empty, which breaks things;
                // we want to treat them as regular stacks where possible
                stack.setCount(1);
            }
            // PORT(26.1.2): CC's public detail registry instead of the internal ItemData helper
            final var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                result.putAll(VanillaDetailRegistries.ITEM_STACK.getDetails(server.registryAccess(), stack));
            }
            if (wasEmptyish) {
                stack.setCount(0);  // it should be a copy we don't care about, but just in case...
                result.put("count", 0);
            }

            return result;
        }

        @Nullable
        private static Object convertNbt(@Nullable Tag nbt) {
            if (nbt == null) return null;
            if (nbt instanceof CompoundTag compound) {
                Map<Object, Object> result = new HashMap<>();
                for (String key : compound.keySet()) {
                    result.put(key, convertNbt(compound.get(key)));
                }
                return result;
            }
            if (nbt instanceof NumericTag number) {
                return number.box();
            }
            if (nbt instanceof ListTag list) {
                List<Object> result = new ArrayList<>();
                for (Tag tag : list) {
                    result.add(convertNbt(tag));
                }
                return result;
            }
            return nbt.asString().orElseGet(nbt::toString);
        }
    }
}
