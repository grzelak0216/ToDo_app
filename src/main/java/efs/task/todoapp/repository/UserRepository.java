package efs.task.todoapp.repository;

import java.util.*;
import java.util.function.Predicate;

import efs.task.todoapp.Base64Utils;


public class UserRepository implements Repository<String, UserEntity> {
    Map<String, UserEntity> usersMap = new HashMap<>();

    @Override
    public String save(UserEntity userEntity) {
        if (null == userEntity) {
            return null;
        }
        String key = userEntity.getUsername();
        if (usersMap.containsKey(key))
            return null;
        else {
            usersMap.put(key, userEntity);
            return key;
        }
    }

    @Override
    public UserEntity query(String s) {
        return usersMap.get(s);
    }

    @Override
    public List<UserEntity> query(Predicate<UserEntity> condition) {
        return null;
    }

    @Override
    public UserEntity update(String s, UserEntity userEntity) {
        return null;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }
}
