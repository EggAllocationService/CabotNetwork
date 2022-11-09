package dev.cabotmc.lobby;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.reflect.jvm.internal.calls.CallerImpl.FieldGetter.Instance;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.instance.block.rule.vanilla.StairsPlacementRule;

public class SexPlacementRule extends BlockPlacementRule {

    public SexPlacementRule( Block block) {
        super(block);
    }

    @Override
    public @NotNull Block blockUpdate(net.minestom.server.instance.@NotNull Instance instance,
            @NotNull Point blockPosition, @NotNull Block currentBlock) {
        return currentBlock;
    }
    @Override
    public @Nullable Block blockPlace(net.minestom.server.instance.@NotNull Instance instance, @NotNull Block block,
            @NotNull BlockFace blockFace, @NotNull Point blockPosition, @NotNull Player pl) {
                Facing facing = this.getFacing(pl);
                BlockFace half = BlockFace.BOTTOM; // waiting for new block faces to be implemented
                String waterlogged = "false"; // waiting for water to be implemented
        
                return block.withProperties(Map.of(
                        "facing", facing.toString(),
                        "half", half.toString(),
                        "shape", "STRAIGHT",
                        "waterlogged", waterlogged));
    }

    private Facing getFacing(@NotNull Player player) {
        float degrees = (player.getPosition().yaw() - 90) % 360;
        if (degrees < 0) {
            degrees += 360;
        }
        if (0 <= degrees && degrees < 45) {
            return Facing.WEST;
        } else if (45 <= degrees && degrees < 135) {
            return Facing.NORTH;
        } else if (135 <= degrees && degrees < 225) {
            return Facing.EAST;
        } else if (225 <= degrees && degrees < 315) {
            return Facing.SOUTH;
        } else { // 315 <= degrees && degrees < 360
            return Facing.WEST;
        }
    }
    private enum Facing {
        NORTH(
                new Vec(0, 0, 1),
                new Vec(0, 0, -1)
        ),
        EAST(
                new Vec(-1, 0, 0),
                new Vec(1, 0, 0)
        ),
        SOUTH(
                new Vec(0, 0, -1),
                new Vec(0, 0, 1)
        ),
        WEST(
                new Vec(1, 0, 0),
                new Vec(-1, 0, 0)
        );

        private final Point front;
        private final Point back;

        Facing(@NotNull Point front, @NotNull Point back) {
            this.front = front;
            this.back = back;
        }

    }
    
}
