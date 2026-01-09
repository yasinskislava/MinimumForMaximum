package rewqazwas.minformax.custom.utility;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

//Author https://github.com/Pashockerr/RaycasterLibNF/tree/1.21.1

public class Raycaster {
    private Raycaster() {}

    public static final Raycaster INSTANCE = new Raycaster();

    public List<EntityHitResult> raycast(Level level, Entity cameraEntity, double distance, boolean ignoreBlocks, boolean ignoreEntities) {
        Predicate<? super Entity> predicate = (e) -> true;
        var entities = level.getEntities(cameraEntity, cameraEntity.getBoundingBox().expandTowards(cameraEntity.getViewVector(1.0f).scale(distance)).inflate(1.0), predicate);

        var eyePos = cameraEntity.getEyePosition();
        var view = cameraEntity.getViewVector(1.0f);
        var target = eyePos.add(view.x * distance, view.y * distance, view.z * distance);

        ArrayList<EntityHitResult> result = new ArrayList<>();

        for(var e : entities) {
            var res = e.getBoundingBox().clip(eyePos, target);
            if(res.isPresent()){
                result.add(new EntityHitResult(e, res.get()));
            }
        }

        if(!ignoreBlocks && !result.isEmpty()){
            HitResult hitResult = cameraEntity.pick(distance, 1.0f, true);
            @SuppressWarnings("unchecked") var tempRes = (ArrayList<EntityHitResult>) result.clone();
            if(hitResult.getType() != HitResult.Type.MISS){
                var distanceToHit = eyePos.distanceTo(hitResult.getLocation());
                for(var entityHitResult : result){
                    var distanceToEntityHit = eyePos.distanceTo(entityHitResult.getLocation());
                    if(distanceToEntityHit > distanceToHit){
                        tempRes.remove(entityHitResult);
                    }
                }
            }
            result = tempRes;
        }

        if(!ignoreEntities && !result.isEmpty()){
            var minDistanceEntityHitResult = result.get(0);
            double currentDistance = eyePos.distanceTo(minDistanceEntityHitResult.getLocation());
            @SuppressWarnings("unchecked") var tempRes = (ArrayList<EntityHitResult>) result.clone();
            for(var entityHitResult : result){
                var dist = eyePos.distanceTo(entityHitResult.getLocation());
                if(dist < currentDistance){
                    tempRes.remove(minDistanceEntityHitResult);
                    minDistanceEntityHitResult = entityHitResult;
                    currentDistance = dist;
                }
            }
            result = tempRes;
        }

        return result;
    }
}