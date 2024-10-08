package ru.timaaos.blocks;

import java.util.*;
import java.util.regex.Pattern;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTarget;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.WorldAccess;
import ru.timaaos.CreateDataAndPlots;

public class DashboardBlockTarget extends DisplayTarget {

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    class BarplotValue{
        public int amount;
        public String id;
    }
    @Override
    public void acceptText(int line, List<MutableText> text, DisplayLinkContext context) {
        try {
            CreateDataAndPlots.LOGGER.info(text.toString());
            String data = text.toString();
            List<BarplotValue> result = new ArrayList<BarplotValue>();
            String[] am = data.split(Pattern.quote("literal{"));
            for (String s : am) {
                String n = s.split("}")[0];
                if (isNumeric(n)) {
                    BarplotValue bv = new BarplotValue();
                    bv.amount = Integer.parseInt(n);
                    result.add(bv);
                }
            }
            am = data.split(Pattern.quote("key='"));
            am = Arrays.copyOfRange(am, 1, am.length);
            int l = 0;
            for (String s : am) {
                String n = s.split("'")[0];
                if(n.startsWith("create.display_source")){
                    BarplotValue bv = result.get(l);
                    if(n.equals("create.display_source.value_list.thousand")){
                        bv.amount *= 1000;
                    }
                    continue;
                }
                BarplotValue bv = result.get(l);
                bv.id = n;
                result.set(l, bv);
                l += 1;
            }

            int[] barHeights = new int[result.size()];
            int[][] barColors = new int[result.size()][];
            String[] barName = new String[result.size()];
            int i = 0;
            for (BarplotValue bv : result) {
                barHeights[i] = bv.amount;
                barName[i] = bv.id;
                long hash = UUID.nameUUIDFromBytes(bv.id.getBytes()).getMostSignificantBits();
                Random random = new Random(hash);
                barColors[i] = new int[]{random.nextInt(255) / 2 + 127, random.nextInt(255) / 2 + 127, random.nextInt(255) / 2 + 127};
                i += 1;
            }
            DashboardBlockEntity be = (DashboardBlockEntity) context.getTargetBlockEntity().getWorld().getBlockEntity(context.getTargetPos());
            be.setBarHeights(barHeights);
            be.setBarColors(barColors);
            be.setBarNames(barName);
            be.getWorld().updateListeners(be.getPos(), be.getCachedState(), be.getCachedState(), Block.NOTIFY_LISTENERS);
        }catch (Exception e){

        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(1000,2, this);
    }
}