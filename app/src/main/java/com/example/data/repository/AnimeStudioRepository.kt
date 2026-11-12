package com.example.data.repository

import com.example.data.local.dao.CharacterDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.SceneDao
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SceneEntity
import kotlinx.coroutines.flow.Flow

class AnimeStudioRepository(
    private val projectDao: ProjectDao,
    private val characterDao: CharacterDao,
    private val sceneDao: SceneDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    fun getProjectById(id: Long): Flow<ProjectEntity?> = projectDao.getProjectByIdFlow(id)

    suspend fun getProject(id: Long): ProjectEntity? = projectDao.getProjectById(id)

    fun getCharactersForProject(projectId: Long): Flow<List<CharacterEntity>> =
        characterDao.getCharactersByProject(projectId)

    suspend fun getCharactersList(projectId: Long): List<CharacterEntity> =
        characterDao.getCharactersListByProject(projectId)

    fun getScenesForProject(projectId: Long): Flow<List<SceneEntity>> =
        sceneDao.getScenesByProject(projectId)

    suspend fun getScenesList(projectId: Long): List<SceneEntity> =
        sceneDao.getScenesListByProject(projectId)

    suspend fun saveProject(project: ProjectEntity): Long {
        return if (project.id == 0L) {
            projectDao.insertProject(project)
        } else {
            projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
            project.id
        }
    }

    suspend fun deleteProject(projectId: Long) {
        projectDao.deleteProjectById(projectId)
    }

    suspend fun saveCharacter(character: CharacterEntity): Long {
        return if (character.id == 0L) {
            characterDao.insertCharacter(character)
        } else {
            characterDao.updateCharacter(character)
            character.id
        }
    }

    suspend fun saveCharacters(characters: List<CharacterEntity>) {
        characterDao.insertCharacters(characters)
    }

    suspend fun deleteCharacter(character: CharacterEntity) {
        characterDao.deleteCharacter(character)
    }

    suspend fun saveScene(scene: SceneEntity): Long {
        return if (scene.id == 0L) {
            sceneDao.insertScene(scene)
        } else {
            sceneDao.updateScene(scene)
            scene.id
        }
    }

    suspend fun saveScenes(scenes: List<SceneEntity>) {
        sceneDao.insertScenes(scenes)
    }

    suspend fun updateScene(scene: SceneEntity) {
        sceneDao.updateScene(scene)
    }

    suspend fun deleteScene(scene: SceneEntity) {
        sceneDao.deleteScene(scene)
    }

    suspend fun deleteSceneById(sceneId: Long) {
        sceneDao.deleteSceneById(sceneId)
    }

    suspend fun replaceAllScenes(projectId: Long, newScenes: List<SceneEntity>) {
        sceneDao.deleteScenesByProject(projectId)
        sceneDao.insertScenes(newScenes)
    }
}
