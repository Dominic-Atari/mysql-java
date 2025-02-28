/**
 * 
 */
package projects.service;

import java.util.List;
import java.util.NoSuchElementException;
import projects.dao.ProjectDao;
import projects.entity.Project;
import projects.exception.DbException;

/**
 * This class implements the service layer in the 3-tier application. The CRUD operations that the
 * application performs are so simple that this class acts mostly as a pass-through from the input
 * layer to the data layer.
 * 
 * @author Promineo
 *
 */
public class ProjectService {
  private ProjectDao projectDao = new ProjectDao();

  /**
   * This method simply calls the DAO class to insert a project row.
   * 
   * @param project The {@link Project} object.
   * @return The Project object with the newly generated primary key value.
   */
  public Project addProject(Project project) {
    return projectDao.insertProject(project);
  }

  /**
   * This method calls the project DAO to retrieve all project rows without accompanying details
   * (materials, steps and categories).
   * 
   * @return A list of project records.
   */
  public List<Project> fetchAllProjects() {
    return projectDao.fetchAllProjects();
  }

  /**
   * This method calls the project DAO to get all project details, including materials, steps, and
   * categories. If the project ID is invalid, it throws an exception.
   * 
   * @param projectId The project ID.
   * @return A Project object if successful.
   * @throws NoSuchElementException Thrown if the project with the given ID does not exist.
   */
  public Project fetchProjectById(Integer projectId) {
    return projectDao.fetchProjectById(projectId).orElseThrow(() -> new NoSuchElementException(
        "Project with project ID=" + projectId + " does not exist."));
  }

  /**
   * This method calls the project DAO to modify the project details row in the project table. The
   * DAO will return {@code true} if the row was modified. It returns {@code false} if the row was
   * not modified (i.e., if an invalid project ID was specified). In this latter case, a DbException
   * is thrown.
   * 
   * @param project The Project object with modified details.
   * @throws DbException Thrown if an invalid project ID is specified.
   */
  public void modifyProjectDetails(Project project) {
    if(!projectDao.modifyProjectDetails(project)) {
      throw new DbException("Project with ID=" + project.getProjectId() + " does not exist.");
    }
  }

  /**
   * This method calls the project DAO to delete the project with the given project ID. If an
   * invalid project ID is given, an exception is thrown.
   * 
   * @param projectId The project ID of the project to delete.
   * @throws DbException Thrown if the project ID does not exist.
   */
  public void deleteProject(Integer projectId) {
    if(!projectDao.deleteProject(projectId)) {
      throw new DbException("Project with ID=" + projectId + " does not exist.");
    }
  }
}
