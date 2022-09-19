package org.simpleframework.aop.aspect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.simpleframework.aop.PointcutLocator;

@AllArgsConstructor
@Getter
public class AspectInfoV1 {
    private int orderIndex;
    private DefaultAspect aspectObject;
}
