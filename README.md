# NationGen
Dominions 6 procedural content generation program.

## To-do
###### List of priority fixes and improvements

- **Refine pretender templates for all gods without realms**

	Should be done in `data/gods/individual/gods-without-realm.txt`. They are mostly nation-specific gods, like the Risen Oracle. Some of them should be fleshed out with more detailed `#chanceinc`, like additional race checks, or theme checks (i.e. cold nations, heat nations, etc).

- **Add mount templates with varying armour types**

	A lot of cavalry is worthless because the mount templates don't have armoured alternatives. These could be done programmatically, but the simplest way would be to copy/paste existing armourless mounts and give them increasing tiers of armour. An example of this is the ettin's helmets, see `data/items/ogre/helmet_ettinling.txt`, the heavier ones with lower and lower `#basechances`.

- **Give mounts the resists of their riders**

	This should probably only be done for riders that have heat/chill/poison auras, so that the mount doesn't die under them, but could also be done more rarely in other cases.

- **Add Glamour communion spells**

	Should probably also give them more frequently to nations with spellsinger, since those are the vanilla nations that have them.

- **Temperature protection**

	Temperature protection in forts for nations with `#idealcold`. The modding command for this does not exist yet.


## Template ideas

- **`#xpshape` and `#labxpshape` units**

	For example, carp units that grow bigger and bigger after killing enough units. This should be done programmatically so that different kinds of xpshapes can be applied to different units without having to manually define the templates every time. There are different flavours of `#xpshape` effects that could be done, i.e. growing in size, or getting more of a certain stat, or changing into more mature versions (like dragon hatchlings to dragons), or gaining more military discipline (formation fighter, morale, leadership, etc).

- **Andramania-based nations**

	This would require graphics work, but now that we have dog people in Dominions 6, that opens new possibilities and themes.

- **Generally, work out all the new modding commands added in Dom6 and make something out of them.**
