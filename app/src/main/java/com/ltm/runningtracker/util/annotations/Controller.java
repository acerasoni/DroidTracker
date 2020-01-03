package com.ltm.runningtracker.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Controller {

  Class[] usedBy();

  Class[] repositoriesAccessed();

}