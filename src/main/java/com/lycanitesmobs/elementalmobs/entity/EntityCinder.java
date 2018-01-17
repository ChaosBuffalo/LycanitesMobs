package com.lycanitesmobs.elementalmobs.entity;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.api.*;
import com.lycanitesmobs.core.entity.EntityCreatureTameable;
import com.lycanitesmobs.core.entity.EntityItemCustom;
import com.lycanitesmobs.core.entity.EntityProjectileRapidFire;
import com.lycanitesmobs.core.entity.ai.*;
import com.lycanitesmobs.core.info.MobDrop;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityCinder extends EntityCreatureTameable implements IMob, IGroupFire, IFusable {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityCinder(World world) {
        super(world);
        
        // Setup:
        this.attribute = EnumCreatureAttribute.UNDEFINED;
        this.defense = 0;
        this.experience = 5;
        this.spawnsInBlock = false;
        this.hasAttackSound = false;
        
        this.setWidth = 0.8F;
        this.setHeight = 1.2F;
        this.setupMob();

        this.stepHeight = 1.0F;
    }

    // ========== Init AI ==========
    @Override
    protected void initEntityAI() {
        super.initEntityAI();
        this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIFollowFuse(this).setLostDistance(16));
        this.tasks.addTask(5, new EntityAIAttackRanged(this).setSpeed(0.75D).setRate(20).setStaminaTime(100).setRange(12.0F).setMinChaseDistance(3.0F));
        this.tasks.addTask(6, this.aiSit);
        this.tasks.addTask(7, new EntityAIFollowOwner(this).setStrayDistance(8).setLostDistance(32));
        this.tasks.addTask(8, new EntityAIWander(this));
        this.tasks.addTask(10, new EntityAIWatchClosest(this).setTargetClass(EntityPlayer.class));
        this.tasks.addTask(11, new EntityAILookIdle(this));

        this.targetTasks.addTask(0, new EntityAITargetOwnerRevenge(this));
        this.targetTasks.addTask(1, new EntityAITargetOwnerAttack(this));
        this.targetTasks.addTask(2, new EntityAITargetRevenge(this));
        this.targetTasks.addTask(2, new EntityAITargetAttack(this).setTargetClass(IGroupIce.class));
        this.targetTasks.addTask(2, new EntityAITargetAttack(this).setTargetClass(IGroupWater.class));
        this.targetTasks.addTask(2, new EntityAITargetAttack(this).setTargetClass(EntitySnowman.class));
        this.targetTasks.addTask(3, new EntityAITargetAttack(this).setTargetClass(EntityPlayer.class));
        this.targetTasks.addTask(3, new EntityAITargetAttack(this).setTargetClass(EntityVillager.class));
        this.targetTasks.addTask(4, new EntityAITargetAttack(this).setTargetClass(IGroupPlant.class));
        this.targetTasks.addTask(6, new EntityAITargetOwnerThreats(this));
		this.targetTasks.addTask(7, new EntityAITargetFuse(this));
    }
    
    // ========== Stats ==========
	@Override
	protected void applyEntityAttributes() {
		HashMap<String, Double> baseAttributes = new HashMap<String, Double>();
		baseAttributes.put("maxHealth", 15D);
		baseAttributes.put("movementSpeed", 0.24D);
		baseAttributes.put("knockbackResistance", 0.0D);
		baseAttributes.put("followRange", 16D);
		baseAttributes.put("attackDamage", 1D);
        super.applyEntityAttributes(baseAttributes);
    }
	
	// ========== Default Drops ==========
	@Override
	public void loadItemDrops() {
        this.drops.add(new MobDrop(new ItemStack(Items.COAL), 0.5F));
        this.drops.add(new MobDrop(new ItemStack(Items.BLAZE_ROD), 0.1F));
        this.drops.add(new MobDrop(new ItemStack(ObjectManager.getItem("EmberCharge")), 0.25F));
	}
    
    
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        
        // Particles:
        if(this.getEntityWorld().isRemote)
	        for(int i = 0; i < 2; ++i) {
	            this.getEntityWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0D, 0.0D, 0.0D);
	            this.getEntityWorld().spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0D, 0.0D, 0.0D);
	        }
    }
    
    
    // ==================================================
    //                      Attacks
    // ==================================================
    // ========== Set Attack Target ==========
    @Override
    public boolean canAttackClass(Class targetClass) {
    	if(targetClass.isAssignableFrom(IGroupFire.class))
    		return false;
        return super.canAttackClass(targetClass);
    }
    
    // ========== Ranged Attack ==========
    @Override
    public void rangedAttack(Entity target, float range) {
    	// Type:
    	List<EntityProjectileRapidFire> projectiles = new ArrayList<EntityProjectileRapidFire>();
    	
    	EntityProjectileRapidFire projectileEntry = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectiles.add(projectileEntry);
    	
    	EntityProjectileRapidFire projectileEntry2 = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectileEntry2.offsetX += 1.0D;
    	projectiles.add(projectileEntry2);
    	
    	EntityProjectileRapidFire projectileEntry3 = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectileEntry3.offsetX -= 1.0D;
    	projectiles.add(projectileEntry3);
    	
    	EntityProjectileRapidFire projectileEntry4 = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectileEntry4.offsetZ += 1.0D;
    	projectiles.add(projectileEntry4);
    	
    	EntityProjectileRapidFire projectileEntry5 = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectileEntry5.offsetZ -= 1.0D;
    	projectiles.add(projectileEntry5);
    	
    	EntityProjectileRapidFire projectileEntry6 = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectileEntry6.offsetY += 1.0D;
    	projectiles.add(projectileEntry6);
    	
    	EntityProjectileRapidFire projectileEntry7 = new EntityProjectileRapidFire(EntityEmber.class, this.getEntityWorld(), this, 15, 3);
    	projectileEntry7.offsetY -= 10D;
    	projectiles.add(projectileEntry7);
    	
    	for(EntityProjectileRapidFire projectile : projectiles) {
	        projectile.setProjectileScale(1f);
	    	
	    	// Y Offset:
	    	projectile.posY -= this.height / 4;
	    	
	    	// Accuracy:
	    	float accuracy = 1.0F * (this.getRNG().nextFloat() - 0.5F);
	    	
	    	// Set Velocities:
	        double d0 = target.posX - this.posX + accuracy;
	        double d1 = target.posY + (double)target.getEyeHeight() - 1.100000023841858D - projectile.posY + accuracy;
	        double d2 = target.posZ - this.posZ + accuracy;
	        float f1 = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
	        float velocity = 1.2F;
	        projectile.setThrowableHeading(d0, d1 + (double)f1, d2, velocity, 6.0F);
	        
	        // Launch:
	        this.playSound(projectile.getLaunchSound(), 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
	        this.getEntityWorld().spawnEntity(projectile);
    	}
    	
        super.rangedAttack(target, range);
    }
    
    
    // ==================================================
  	//                     Abilities
  	// ==================================================
    @Override
    public boolean isFlying() { return true; }
    
    
    // ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }


    // ==================================================
    //                     Equipment
    // ==================================================
    public int getNoBagSize() { return 0; }
    public int getBagSize() { return 5; }
    
    
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public boolean isPotionApplicable(PotionEffect potionEffect) {
        if(ObjectManager.getPotionEffect("Penetration") != null)
            if(potionEffect.getPotion() == ObjectManager.getPotionEffect("Penetration")) return false;
        super.isPotionApplicable(potionEffect);
        return true;
    }
    
    @Override
    public boolean canBurn() { return false; }
    
    @Override
    public boolean waterDamage() { return true; }
    
    
    // ==================================================
   	//                    Taking Damage
   	// ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
    	if(damageSrc.isFireDamage())
    		return 0F;
    	else return super.getDamageModifier(damageSrc);
    }
    
    
    // ==================================================
   	//                       Drops
   	// ==================================================
    // ========== Apply Drop Effects ==========
    /** Used to add effects or alter the dropped entity item. **/
    @Override
    public void applyDropEffects(EntityItemCustom entityitem) {
    	entityitem.setCanBurn(false);
    }
    
    
    // ==================================================
    //                   Brightness
    // ==================================================
    @Override
    public float getBrightness() {
        return 1.0F;
    }
    
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender() {
        return 15728880;
    }


	// ==================================================
	//                      Fusion
	// ==================================================
	protected IFusable fusionTarget;

	@Override
	public IFusable getFusionTarget() {
		return this.fusionTarget;
	}

	@Override
	public void setFusionTarget(IFusable fusionTarget) {
		this.fusionTarget = fusionTarget;
	}

	@Override
	public Class getFusionClass(IFusable fusable) {
		if(fusable instanceof EntityGeonach) {
			return EntityVolcan.class;
		}
		return null;
	}
}