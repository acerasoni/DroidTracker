package com.ltm.runningtracker.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * This annotation defines the Presenter within the ViewModel. Methods tagged with
 * Presenter require the list of activites they are used by, and the list of repositories
 * they require access to.
 *
 * @see com.ltm.runningtracker.android.activity.viewmodel.ActivityViewModel
 */
@Target(ElementType.METHOD)
public @interface Presenter {

  Class[] usedBy();

  Class[] repositoriesAccessed();

}
