package com.gctn.tconstruct.smeltery.menu; // 声明此类所属的包，使其与对应功能模块保持一致。

import com.gctn.tconstruct.smeltery.SmelteryRegistration; // 引入 com.gctn.tconstruct.smeltery.SmelteryRegistration，供本文件使用。
import com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity; // 引入 com.gctn.tconstruct.smeltery.block.entity.SmelteryControllerBlockEntity，供本文件使用。
import com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes; // 引入 com.gctn.tconstruct.smeltery.recipe.SmelteryRecipes，供本文件使用。
import net.minecraft.network.RegistryFriendlyByteBuf; // 引入 net.minecraft.network.RegistryFriendlyByteBuf，供本文件使用。
import net.minecraft.world.*; // 引入 net.minecraft.world.*，供本文件使用。
import net.minecraft.world.entity.player.*; // 引入 net.minecraft.world.entity.player.*，供本文件使用。
import net.minecraft.world.inventory.*; // 引入 net.minecraft.world.inventory.*，供本文件使用。
import net.minecraft.world.item.ItemStack; // 引入 net.minecraft.world.item.ItemStack，供本文件使用。

/**
 * 为每位玩家分别管理大型控制器库存的分页。
 * 只同步 27 个物品槽位，切页时重新映射服务端的 Container 视图。
 * 原版菜单数据包会截断整数，因此槽位总数拆分为两个 16 位值同步。
 */
public class SmelteryMenu extends AbstractContainerMenu { // 定义 SmelteryMenu 的实现类型，具体职责见上方说明。
    public static final int VISIBLE_SLOTS = 27, DATA_COUNT = 32; // 设置 VISIBLE_SLOTS 为 27, DATA_COUNT = 32。
    public static final int PREVIOUS = 10000, NEXT = 10001; // 设置 PREVIOUS 为 10000, NEXT = 10001。
    private final SmelteryControllerBlockEntity controller; // 声明 controller，保存后续流程使用的状态或依赖。
    private final ContainerData data; // 声明 data，保存后续流程使用的状态或依赖。
    private final Container view; // 声明 view，保存后续流程使用的状态或依赖。
    private int page; // 声明 page，保存后续流程使用的状态或依赖。

    public SmelteryMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) { // 定义 SmelteryMenu 的调用入口，参数和具体处理见下方实现。
        this(id, inventory, inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof SmelteryControllerBlockEntity c ? c : null, false); // 调用 this，执行当前流程所需的处理。
    } // 结束当前作用域。
    public SmelteryMenu(int id, Inventory inventory, SmelteryControllerBlockEntity controller) { this(id, inventory, controller, true); } // 定义 SmelteryMenu 的调用入口；调用 this，执行当前流程所需的处理。
    private SmelteryMenu(int id, Inventory inventory, SmelteryControllerBlockEntity controller, boolean server) { // 定义 SmelteryMenu 的调用入口，参数和具体处理见下方实现。
        super(SmelteryRegistration.MENU.get(), id); // 读取指定条目或延迟注册对象。
        this.controller = controller; // 设置 this.controller 为 controller。
        this.view = server ? new PageContainer() : new SimpleContainer(VISIBLE_SLOTS); // 设置 this.view 为 server ? new PageContainer() : new SimpleContainer(VISIBLE_SLOTS)。
        this.data = server ? new ContainerData() { // 设置 this.data 为 server ? new ContainerData() {。
            @Override public int get(int index) { // 实现父类或接口约定；定义 get 的调用入口，参数和具体处理见下方实现。
                return switch (index) { // 返回 switch (index) {，将当前分支的结果交给调用者。
                    case 0 -> controller.getContainerSize() & 65535; // 定义 case 0 分支，调用 getContainerSize，执行当前流程所需的处理。
                    case 1 -> controller.getContainerSize() >>> 16; // 定义 case 1 分支，调用 getContainerSize，执行当前流程所需的处理。
                    case 2 -> controller.fuel(); // 定义 case 2 分支，调用 fuel，执行当前流程所需的处理。
                    case 3 -> page; // 定义 case 3 分支，产生此情况对应的结果。
                    case 4 -> controller.isFormed() ? 1 : 0; // 定义 case 4 分支，调用 isFormed，执行当前流程所需的处理。
                    default -> controller.progress(page * VISIBLE_SLOTS + index - 5); // 定义 default 分支，调用 progress，执行当前流程所需的处理。
                }; // 结束当前作用域并完成本次表达式或声明。
            } // 结束当前作用域。
            @Override public void set(int index, int value) {} // 实现父类或接口约定；定义 set 的调用入口；此处无需额外处理。
            @Override public int getCount() { return DATA_COUNT; } // 实现父类或接口约定；定义 getCount 的调用入口；返回 DATA_COUNT，将当前分支的结果交给调用者。
        } : new SimpleContainerData(DATA_COUNT); // 向当前声明或调用传入 } : new SimpleContainerData(DATA_COUNT)。
        for (int i = 0; i < VISIBLE_SLOTS; i++) { // 遍历 int i = 0; i < VISIBLE_SLOTS; i++，逐项执行当前处理。
            final int index = i; // 设置 index 为 i。
            addSlot(new Slot(view, i, 8 + i % 9 * 18, 28 + i / 9 * 18) { // 向菜单登记物品槽位及其交互规则。
                @Override public boolean isActive() { return currentPage() * VISIBLE_SLOTS + index < totalSlots(); } // 实现父类或接口约定；定义 isActive 的调用入口；返回 currentPage() * VISIBLE_SLOTS + index < totalSlots()，将当前分支的结果交给调用者。
                @Override public boolean mayPlace(ItemStack stack) { return isActive() && SmelteryRecipes.melting(inventory.player.level(), stack) != null; } // 实现父类或接口约定；定义 mayPlace 的调用入口；返回 isActive() && SmelteryRecipes.melting(inventory.player.level(), stack) != null，将当前分支的结果交给调用者。
                @Override public int getMaxStackSize() { return 1; } // 实现父类或接口约定；定义 getMaxStackSize 的调用入口；返回 1，将当前分支的结果交给调用者。
            }); // 结束当前作用域并完成本次表达式或声明。
        } // 结束当前作用域。
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18)); // 遍历 int row = 0; row < 3; row++) for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column …，逐项执行当前处理。
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 198)); // 遍历 int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 198)，逐项执行当前处理。
        addDataSlots(data); // 挂接需要在客户端与服务端之间同步的菜单数据。
    } // 结束当前作用域。
    public SmelteryControllerBlockEntity controller() { return controller; } // 定义 controller 的调用入口；返回 controller，将当前分支的结果交给调用者。
    public int totalSlots() { return (data.get(0) & 65535) | data.get(1) << 16; } // 定义 totalSlots 的调用入口；返回 (data.get(0) & 65535) | data.get(1) << 16，将当前分支的结果交给调用者。
    public int currentPage() { return data.get(3); } // 定义 currentPage 的调用入口；返回 data.get(3)，将当前分支的结果交给调用者。
    public int pages() { return Math.max(1, (totalSlots() + VISIBLE_SLOTS - 1) / VISIBLE_SLOTS); } // 定义 pages 的调用入口；返回 Math.max(1, (totalSlots() + VISIBLE_SLOTS - 1) / VISIBLE_SLOTS)，将当前分支的结果交给调用者。
    public int fuel() { return data.get(2); } // 定义 fuel 的调用入口；返回 data.get(2)，将当前分支的结果交给调用者。
    public int progress(int visibleSlot) { return data.get(5 + visibleSlot); } // 定义 progress 的调用入口；返回 data.get(5 + visibleSlot)，将当前分支的结果交给调用者。
    @Override public void broadcastChanges() { // 实现父类或接口约定；定义 broadcastChanges 的调用入口，参数和具体处理见下方实现。
        if (controller != null && !controller.getLevel().isClientSide) page = Math.min(page, Math.max(0, (controller.getContainerSize() - 1) / VISIBLE_SLOTS)); // 检查 controller != null && !controller.getLevel().isClientSide；按条件决定是否执行本分支。
        super.broadcastChanges(); // 调用 broadcastChanges，执行当前流程所需的处理。
    } // 结束当前作用域。
    @Override public boolean stillValid(Player player) { return controller != null && controller.isFormed() && controller.stillValid(player); } // 实现父类或接口约定；定义 stillValid 的调用入口；返回 controller != null && controller.isFormed() && controller.stillValid(player)，将当前分支的结果交给调用者。
    @Override public boolean clickMenuButton(Player player, int button) { // 实现父类或接口约定；定义 clickMenuButton 的调用入口，参数和具体处理见下方实现。
        if (!stillValid(player) || player.level().isClientSide) return false; // 检查 !stillValid(player) || player.level().isClientSide；按条件决定是否执行本分支。
        if (button == PREVIOUS && page > 0) page--; // 检查 button == PREVIOUS && page > 0；按条件决定是否执行本分支。
        else if (button == NEXT && page + 1 < pages()) page++; // 前一条件不成立时继续检查本分支条件。
        else if (button >= 0 && button < controller.tank().contents().size()) controller.tank().select(button); // 前一条件不成立时继续检查本分支条件。
        else return false; // 处理前述条件均不满足时的替代分支。
        broadcastChanges(); // 调用 broadcastChanges，执行当前流程所需的处理。
        return true; // 返回 true，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    @Override public ItemStack quickMoveStack(Player player, int index) { // 实现父类或接口约定；定义 quickMoveStack 的调用入口，参数和具体处理见下方实现。
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY; // 检查 index < 0 || index >= slots.size() || !slots.get(index).hasItem()；按条件决定是否执行本分支。
        Slot slot = slots.get(index); // 设置 slot 为 slots.get(index)。
        ItemStack stack = slot.getItem(), original = stack.copy(); // 设置 stack 为 slot.getItem(), original = stack.copy()。
        if (index < VISIBLE_SLOTS) { if (!moveItemStackTo(stack, VISIBLE_SLOTS, slots.size(), true)) return ItemStack.EMPTY; } // 检查 index < VISIBLE_SLOTS；按条件决定是否执行本分支。
        else if (!moveItemStackTo(stack, 0, VISIBLE_SLOTS, false)) return ItemStack.EMPTY; // 前一条件不成立时继续检查本分支条件。
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged(); // 检查 stack.isEmpty()；按条件决定是否执行本分支。
        slot.onTake(player, stack); // 调用 onTake，执行当前流程所需的处理。
        return original; // 返回 original，将当前分支的结果交给调用者。
    } // 结束当前作用域。
    private class PageContainer implements Container { // 定义 PageContainer 的实现类型，具体职责见上方说明。
        private int real(int index) { return page * VISIBLE_SLOTS + index; } // 定义 real 的调用入口；返回 page * VISIBLE_SLOTS + index，将当前分支的结果交给调用者。
        private boolean available(int index) { return index >= 0 && index < VISIBLE_SLOTS && real(index) < controller.getContainerSize(); } // 定义 available 的调用入口；返回 index >= 0 && index < VISIBLE_SLOTS && real(index) < controller.getContainerSize()，将当前分支的结果交给调用者。
        @Override public int getContainerSize() { return VISIBLE_SLOTS; } // 实现父类或接口约定；定义 getContainerSize 的调用入口；返回 VISIBLE_SLOTS，将当前分支的结果交给调用者。
        @Override public int getMaxStackSize() { return 1; } // 实现父类或接口约定；定义 getMaxStackSize 的调用入口；返回 1，将当前分支的结果交给调用者。
        @Override public boolean isEmpty() { for (int i = 0; i < VISIBLE_SLOTS; i++) if (!getItem(i).isEmpty()) return false; return true; } // 实现父类或接口约定；定义 isEmpty 的调用入口；遍历 int i = 0; i < VISIBLE_SLOTS; i++) if (!getItem(i).isEmpty()，逐项执行当前处理。
        @Override public ItemStack getItem(int index) { return available(index) ? controller.getItem(real(index)) : ItemStack.EMPTY; } // 实现父类或接口约定；定义 getItem 的调用入口；返回 available(index) ? controller.getItem(real(index)) : ItemStack.EMPTY，将当前分支的结果交给调用者。
        @Override public ItemStack removeItem(int index, int count) { return available(index) ? controller.removeItem(real(index), count) : ItemStack.EMPTY; } // 实现父类或接口约定；定义 removeItem 的调用入口；返回 available(index) ? controller.removeItem(real(index), count) : ItemStack.EMPTY，将当前分支的结果交给调用者。
        @Override public ItemStack removeItemNoUpdate(int index) { return available(index) ? controller.removeItemNoUpdate(real(index)) : ItemStack.EMPTY; } // 实现父类或接口约定；定义 removeItemNoUpdate 的调用入口；返回 available(index) ? controller.removeItemNoUpdate(real(index)) : ItemStack.EMPTY，将当前分支的结果交给调用者。
        @Override public void setItem(int index, ItemStack stack) { if (available(index)) controller.setItem(real(index), stack); } // 实现父类或接口约定；定义 setItem 的调用入口；检查 available(index)；按条件决定是否执行本分支。
        @Override public void setChanged() { controller.setChanged(); } // 实现父类或接口约定；定义 setChanged 的调用入口；标记数据已变化，供区块保存或菜单同步使用。
        @Override public boolean stillValid(Player player) { return SmelteryMenu.this.stillValid(player); } // 实现父类或接口约定；定义 stillValid 的调用入口；返回 SmelteryMenu.this.stillValid(player)，将当前分支的结果交给调用者。
        @Override public boolean canPlaceItem(int index, ItemStack stack) { return available(index) && controller.canPlaceItem(real(index), stack); } // 实现父类或接口约定；定义 canPlaceItem 的调用入口；返回 available(index) && controller.canPlaceItem(real(index), stack)，将当前分支的结果交给调用者。
        @Override public void clearContent() { for (int i = 0; i < VISIBLE_SLOTS; i++) setItem(i, ItemStack.EMPTY); } // 实现父类或接口约定；定义 clearContent 的调用入口；遍历 int i = 0; i < VISIBLE_SLOTS; i++) setItem(i, ItemStack.EMPTY，逐项执行当前处理。
    } // 结束当前作用域。
} // 结束当前作用域。
