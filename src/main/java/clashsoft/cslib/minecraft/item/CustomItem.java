package clashsoft.cslib.minecraft.item;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import clashsoft.cslib.minecraft.client.icon.IIconSupplier;
import clashsoft.cslib.minecraft.client.icon.IconSupplier;
import clashsoft.cslib.minecraft.client.icon.MultiIconSupplier;
import clashsoft.cslib.minecraft.item.meta.ISubItem;
import clashsoft.cslib.minecraft.lang.I18n;
import clashsoft.cslib.util.CSArrays;
import clashsoft.cslib.util.CSString;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

/**
 * The Class CustomItem.
 */
public class CustomItem extends Item
{
	public static final String	FORCEHIDE	= "%&";
	
	public String[]				names;
	public IIconSupplier		iconSupplier;
	
	public CreativeTabs[]		tabs;
	
	public boolean[]			enabled;
	
	public List<ISubItem>		subItems;
	public List<ISubItem>		shownSubItems;
	
	/**
	 * Instantiates a new custom item.
	 * 
	 * @param subItems
	 *            the sub items
	 * @param shownSubItems
	 *            the displaylist
	 */
	public CustomItem(List<ISubItem> subItems, List<ISubItem> shownSubItems)
	{
		this.subItems = subItems;
		this.shownSubItems = shownSubItems;
		
		String[] iconNames = new String[subItems.size()];
		for (int i = 0; i < subItems.size(); i++)
		{
			ISubItem item = subItems.get(i);
			if (item != null)
			{
				iconNames[i] = item.getIconName();
			}
		}
		this.iconSupplier = new MultiIconSupplier(iconNames);
	}
	
	/**
	 * Instantiates a new custom item.
	 * 
	 * @param names
	 *            the names
	 * @param iconNames
	 *            the icon names
	 * @param descriptions
	 *            the descriptions
	 */
	public CustomItem(String[] names, Object icons, CreativeTabs[] tabs)
	{
		this.names = names;
		this.iconSupplier = IconSupplier.create(icons);
		this.tabs = tabs;
		this.enabled = new boolean[this.names.length];
		Arrays.fill(this.enabled, true);
		
		this.setHasSubtypes(names.length > 1);
	}
	
	public CustomItem(String name, String iconName)
	{
		this(CSArrays.create(name), iconName, null);
	}
	
	@Override
	public CreativeTabs[] getCreativeTabs()
	{
		if (this.tabs == null)
		{
			this.tabs = new CreativeTabs[] { this.getCreativeTab() };
		}
		return this.tabs;
	}
	
	public CreativeTabs getCreativeTab(int metadata)
	{
		if (this.tabs == null)
		{
			return this.getCreativeTab();
		}
		return this.tabs[metadata % this.tabs.length];
	}
	
	public CustomItem addSubItem(ISubItem metaItem)
	{
		if (metaItem != null)
		{
			this.subItems.add(metaItem);
			if (metaItem.isEnabled())
			{
				this.shownSubItems.add(metaItem);
			}
		}
		return this;
	}
	
	/**
	 * Checks if this CustomItem defines its meta-item properties with a
	 * MetaItem object.
	 * 
	 * @return true, if successful
	 */
	public boolean hasItemMetadataList()
	{
		return this.subItems != null && this.shownSubItems != null;
	}
	
	/**
	 * Sets the <code>metadata</code> values to be shown in the creative
	 * inventory or not.
	 * 
	 * @param metadata
	 *            the metadata
	 * @return the custom item
	 */
	public CustomItem setMetadataEnabled(boolean enabled, int... metadata)
	{
		if (metadata != null)
		{
			for (int i : metadata)
			{
				this.enabled[i] = enabled;
			}
		}
		return this;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (this.hasItemMetadataList())
		{
			return this.subItems.get(stack.getItemDamage()).getName();
		}
		else
		{
			return getUnlocalizedName(this, this.names, stack);
		}
	}
	
	public static String getUnlocalizedName(Item item, String[] names, ItemStack stack)
	{
		return item.getUnlocalizedName() + "." + names[stack.getItemDamage()];
	}
	
	@Override
	public IIcon getIconFromDamage(int damage)
	{
		return this.iconSupplier.getIcon(damage);
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.iconSupplier.registerIcons(iconRegister);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean flag)
	{
		if (this.hasItemMetadataList())
		{
			Collection<String> s = this.subItems.get(stack.getItemDamage()).getDescription();
			list.addAll(s);
		}
		
		addInformation(this, stack, list);
	}
	
	public static void addInformation(Item item, ItemStack stack, List<String> list)
	{
		String key = item.getUnlocalizedName(stack) + ".desc";
		String desc = I18n.getString(key);
		if (desc != key && !desc.isEmpty())
		{
			list.addAll(CSString.lineList(desc));
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List subItems)
	{
		if (this.hasItemMetadataList())
		{
			for (ISubItem subitem : this.shownSubItems)
			{
				subItems.add(subitem.asStack());
			}
		}
		else
		{
			for (int i = 0; i < this.names.length; i++)
			{
				if (this.enabled[i] && (tab == null || tab == this.getCreativeTab(i)))
				{
					subItems.add(new ItemStack(this, 1, i));
				}
			}
		}
	}
}
