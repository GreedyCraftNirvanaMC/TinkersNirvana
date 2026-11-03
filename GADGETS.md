# Gadgets

The Gadgets creative tab contains the ported utilities and decorative blocks.
All added item names and tooltips support English and Simplified Chinese.

Eight snowballs surrounding glowstone dust make eight Glowballs. They stack to
sixteen and throw with Antique's speed, scatter, and snowball sound. A successful
impact places a thin, noncolliding light patch on the struck face; an entity hit
tries to place one at the entity's position without dealing damage. The patch
emits light level 14. It requires a solid supporting face, tries another face if
the preferred face is unavailable, and cannot replace water or a solid block.
Removing support removes the patch. It drops nothing; picking it gives a
Glowball. The projectile uses native impact events, so a cancelled impact does
not place a patch or consume the flying projectile. Creative use preserves the
held stack; ordinary use consumes one ball.

## 晾干与食品

物品架和晾干架可挂在墙面、地面或天花板上；右击放入一件物品，再次右击取回。晾干架可制作十种肉干、五种史莱姆凝露、皮革、干燥黏土和干燥黏土砖块。肉干与凝露需要五分钟，黏土球需要两分钟。漏斗或管道只可向空架放入一件物品，并从晾干架取出成品；成品就绪时比较器输出 15。`gadgets.dryingTimeMultiplier` 调整时长，`gadgets.dryLeather` 控制熟肉晾成皮革。

## 黏液装备

黏液弹弓有五种常规颜色。站在地上并瞄准方块，按住使用键蓄力，松手后向反方向弹射。黏液靴免除正常弹跳时的摔落伤害；潜行会停止弹跳并减轻摔落伤害。装备不提供额外护甲值。

手持背架右击生物可将其背在身上。多个乘客需要相应数量的背架，默认最多三个；负重会降低移动速度。脱下背架会逐个放下乘客。`gadgets.limitPiggybackpack` 可将目标限制为玩家和可拴绳生物，`gadgets.maxPiggybackPassengers` 调整人数。条件不满足时操作不会扣物品。

## 黏液通道与 EFLN

一块凝固黏液烧炼成三个同色黏液通道。按放置落点选择八个流向之一，潜行放置反转流向；地板、墙面和天花板之间可形成内外转角。通道可运输生物和物品，红石信号会停止流动。通道会扑灭火焰、防止溺水和摔落；其中到期的掉落物会延长寿命。

燧石加火药或硫粉可制作一枚 EFLN。它沿球形范围逐步破坏抗爆性允许的方块，保留正常掉落，并推开附近实体。它使用原生爆炸取消与范围事件；分批处理不会强制加载区块。光球和 EFLN 均支持发射器。

## 精美展示框与意面

精美展示框有宝石、铝黄铜、钴、阿迪特、玛玉灵、金和透明七种材质，沿用原版旋转、地图与取下物品操作。透明展示框放入物品后隐藏框体，取空后重新显示边框。

硬意面可在沙漠神殿、丛林神庙和要塞走廊宝箱中罕见获得。在浇铸台上浸泡三桶水十五分钟，晾干十五分钟，再烧炼成妈妈的意面。意面可食用一百次，耗尽后仍保留物品。在装配台修复模式中用小麦逐次补充；甜菜汤添加酱汁并返还碗，随后三种不同熟肉添加肉料。意面系列沿用原版隐藏创造栏的设定。

数据包或 KubeJS 可添加晾干配方，格式见 [TOOL_API.md](TOOL_API.md)。

Craft a Wooden Hopper from five planks surrounding a wooden chest in the normal
hopper pattern. It holds one stack and transfers items at half the normal hopper
rate. It collects dropped items and pulls from inventories above, including
sided inventories, and outputs downward or horizontally. Redstone does not lock
it. Its screen keeps Antique's centered single slot and original texture. A
comparator reads the stored stack, breaking it drops its contents, and it can be
used as furnace fuel for 300 ticks.

Five sugar canes in an X-shaped recipe make three Punji Sticks. Attach them to
any solid face, including walls and ceilings. Living entities touching them take
damage and briefly receive Slowness II; falling contact is more dangerous. Nearby
sticks on the same face connect visually, including diagonal neighbors. Removing
the supporting face drops the sticks as items.

Wooden Rails use the normal rail shape, speed, curves, and slopes, with wooden
sounds and lower hardness. Six planks around a wooden rod make four rails.
Replace the rod with a wooden trapdoor to craft four Wooden Rails with a
Trapdoor. While a hopper minecart is on a trapdoor rail, it can unload one item
per tick into the inventory below, respecting that inventory's top-side item
handler. Plain rails and chest minecarts do not perform this unloading. Full or
unavailable inventories keep the cargo in the cart.

Firewood, Lavawood, and Mud Bricks each have full blocks, slabs, and stairs.
Craft Firewood from Lavawood and two blaze powders. Three full blocks make six
slabs; the stair-shaped recipe using six blocks makes four stairs. Two Mud Bricks
(items) can also make one Mud Bricks Slab. Double slabs drop two slabs.

Firewood and Lavawood blocks and slabs emit light level 7; their stairs retain
Antique's unlit stair behavior. All six wood variants burn for 300 ticks. Axes
work on the wood families, while shovels work on Mud Bricks. The ported metal
storage blocks require a pickaxe of any tier and can form beacon bases. Full
copper blocks retain beacon support through oxidation and waxing.

Two stone or cobblestone blocks in a vertical crafting column make four Stone
Rods. A rod below coal or charcoal makes four Stone Torches. Torches can stand on
the ground or attach to a wall and produce light level 14 with normal flame
particles. The ladder-shaped recipe using seven Stone Rods makes three Stone
Ladders. Stone torches and ladders use stone placement/breaking sounds and native
Minecraft attachment rules. Stone Rods are exposed through `c:rods/stone`.

Brownstone has all twelve Antique styles, each with full blocks, slabs, and
stairs. Walking on them boosts horizontal movement while retaining vertical
movement; the original water-specific multiplier is preserved. No lasting
movement attribute is added. Three Rough Brownstone blocks are crafted from
four sandstone blocks and two redstone dust. Smelt rough blocks into smooth
blocks, then use the original shapeless decoration cycle to reach the other
styles. Smelting bricks produces cracked bricks. Three blocks make six slabs;
six blocks in the stair recipe make four stairs. Double slabs drop two slabs.

The native modern blocks provide stair corners, slab merging, waterlogging,
support removal, and torch/ladder placement. These building blocks are separate
from the seared blocks used in Smeltery and Tinker Tank structures.
