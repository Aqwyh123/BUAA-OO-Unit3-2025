package com.oocourse.spec2.main;

public interface OfficialAccountInterface {

    /*@ public instance model int ownerId;
      @ public instance model int id;
      @ public instance model non_null String name;
      @ public instance model non_null PersonInterface[] followers;
      @ public instance model non_null int[] articles;
      @ public instance model non_null int[] contributions;
      @*/

    //@ ensures \result == ownerId;
    public /*@ pure @*/ int getOwnerId();

    /*@ public normal_behavior
      @ requires !containsFollower(person);
      @ assignable followers;
      @ ensures containsFollower(person);
      @ ensures (\exists int i; 0 <= i && i < followers.length; followers[i].getId() == person.getId() && contributions[i] == 0);
      @*/
    public /*@ safe @*/ void addFollower(/*@ non_null @*/PersonInterface person);

    //@ ensures \result == (\exists int i; 0 <= i && i < followers.length; followers[i].getId() == person.getId());
    public /*@ pure @*/ boolean containsFollower(PersonInterface person);

    /*@ public normal_behavior
      @ requires !containsArticle(id);
      @ assignable articles, contributions;
      @ ensures containsArticle(id) &&
      @         (\exists int i; 0 <= i && i < followers.length; followers[i] == person && contributions[i] == \old(contributions[i]) + 1);
      @*/
    public /*@ safe @*/ void addArticle(/*@ non_null @*/PersonInterface person, int id);

    //@ ensures \result == (\exists int i; 0 <= i && i < articles.length; articles[i] == id);
    public /*@ pure @*/ boolean containsArticle(int id);

    /*@ public normal_behavior
      @ requires containsArticle(id);
      @ assignable articles;
      @ ensures !containsArticle(id);
      @*/
    public /*@ safe @*/ void removeArticle(int id);

    /*@ public normal_behavior
      @ assignable \nothing;
      @ ensures (\result == (\min int bestId;
      @                         (\exists int i; 0 <= i && i < followers.length; followers[i].id == bestId &&
      @                             (\forall int j; 0 <= j && j < followers.length; contributions[i] >= contributions[j]));
      @                         bestId));
     */
    public /*@ pure @*/ int getBestContributor();

}
