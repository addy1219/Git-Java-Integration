import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Jgit
{
    public static void main(String[] args)
    {
        String repoPath = "/home/adityasrivastava/Documents/jgit-repo/README.md";
        try {
            GitLabApi gitLabApi = GitLabApi.oauth2Login("http://gitlab.localhost.com", "root", "root");
            CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("root", "root");

            Git git = cloneRepo(credentialsProvider);

              while(true)
              {
                  Scanner scanner = new Scanner(System.in);
                  int choice = scanner.nextInt();

                  switch (choice)
                  {
                      case 1:
                          init();
                          break;
                      case 2:
                          commit(git);
                          break;
                      case 3:
                          push(git, credentialsProvider);
                          break;
                      case 4:
                          pull(git, credentialsProvider);
                          break;
                      case 5:
                          newProject(gitLabApi);
                          break;
                      case 6:
                          newUser(gitLabApi);
                          break;
                      case 7:
                          branchOps(git, credentialsProvider);
                          break;
                      case 8:
                          getLatestLocalCommit(git);
                          break;
                      case 9:
                          getLatestRemoteCommit(git);
                          break;
                      case 10:
                          getRepoMeta(git);
                          break;
                      case 11:
                          add(git, true);
                          break;
                      case 12:
                          reset(git, repoPath);
                          break;
                      case 13:
                          merge(git, credentialsProvider);
                          break;
                      case 14:
                          createTag(git, credentialsProvider);
                          break;
                      case 15:
                          hasLocalChanges(git);
                          break;
                      default:
                          System.exit(0);
                  }
              }
        }
        catch (GitAPIException | URISyntaxException | GitLabApiException | IOException e) {
            e.printStackTrace();
        }
    }
    public static void init() throws IOException, GitAPIException {
        File directory = new File("/home/adityasrivastava/Documents/jgit-repo");

        new File(directory,".git/objects").mkdirs();
        new File(directory,".git/refs/heads").mkdirs();

        File head = new File(directory,".git/HEAD");

        FileWriter fileWriter = new FileWriter(head);
        fileWriter.append("ref:refs/heads/master");
        fileWriter.close();

        Git git = Git.init().setDirectory(directory).call();
        System.out.println(git.getRepository());
    }
    public static void commit(Git git) throws GitAPIException, IOException {
        add(git, true);

        CommitCommand commit = git.commit();
        commit.setMessage("Initial commit...").call();

        RevWalk walk = new RevWalk( git.getRepository() );
        ObjectId head = git.getRepository().resolve( "HEAD" );
            RevCommit lastCommit = walk.parseCommit( head );
        System.out.println(lastCommit.getId());
    }
    public static String getLatestLocalCommit(Git git) throws IOException
    {
        RevWalk walk = new RevWalk(git.getRepository());
        ObjectId head = git.getRepository().resolve("HEAD");
        RevCommit lastCommit = walk.parseCommit(head);
        System.out.println(lastCommit.getId());

        PersonIdent personIdent = lastCommit.getAuthorIdent();
        System.out.println(personIdent.getName());
        System.out.println(personIdent.getEmailAddress());
        System.out.println(personIdent.getWhen());

        return lastCommit.getId().getName();
    }

    public static void getLatestRemoteCommit(Git git) throws GitAPIException, IOException {
        Collection<Ref> collection = git.lsRemote().call();
        System.out.println(collection.iterator().next().getObjectId().getName());

        RevWalk walk = new RevWalk(git.getRepository());
        RevCommit lastCommit = walk.parseCommit(collection.iterator().next().getObjectId());

        PersonIdent personIdent = lastCommit.getAuthorIdent();
        System.out.println(personIdent.getName());
        System.out.println(personIdent.getEmailAddress());
        System.out.println(personIdent.getWhen());
    }
    public static Git cloneRepo(CredentialsProvider credentialsProvider) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI("https://github.com/addy1219/Java.git")
                .setCredentialsProvider(credentialsProvider)
                .setDirectory(new File("/home/adityasrivastava/Documents/jgit-repo"))
                .call();
        System.out.println(git.getRepository());
        return git;
    }
    public static void getRepoMeta(Git git) throws IOException, GitAPIException {
            boolean localChanges = false;
            String lastCommit = getLatestLocalCommit(git);
            String currentBranch = git.getRepository().getFullBranch();

            Status status = git.status().call();

            Set<String> uncommittedChanges = status.getUncommittedChanges();
            if( ! uncommittedChanges.isEmpty() )
                localChanges = true;

            AddRepoMeta repoMeta = new AddRepoMeta();
            repoMeta.setCommitId(lastCommit);
            repoMeta.setCurrentBranch(currentBranch);
            repoMeta.setLocalChanges(localChanges);
            repoMeta.setSyncInProgress(false);

            System.out.println(repoMeta.getCommitId());
            System.out.println(repoMeta.getCurrentBranch());
            System.out.println(repoMeta.isLocalChanges());
    }

    public static void add(Git git, boolean file) throws GitAPIException {
        if( file )
            git.add().addFilepattern(".").call();
        else
            listOfFiles(null, git, null );
    }

    private static Git listOfFiles(File filePath, Git git, String projectPath) throws GitAPIException
    {
        if( !filePath.isDirectory() )
        {
            git.add().addFilepattern(filePath.getAbsolutePath().replace(projectPath + File.separator, "")).call();
            return git;
        }
        File[] filesList = filePath.listFiles();
        if( filesList == null )
        {
            return git;
        }
        for( File file : filesList )
        {
            if( file.isFile() )
                git.add().addFilepattern(filePath.getAbsolutePath().replace(projectPath + File.separator, "")).call();
            else
                listOfFiles(file, git, projectPath);
        }
        return git;
    }

    public static void reset(Git git, String repoPath) throws GitAPIException {
        Ref ref = git.reset().addPath(repoPath).call();
        ResetCommand reset = git.reset();
        reset.setRef(ref.getName());
        reset.setMode(ResetCommand.ResetType.HARD);
        reset.call();
    }

    public static void push(Git git, CredentialsProvider credentialsProvider) throws GitAPIException, URISyntaxException {

        CheckoutCommand coCmd = git.checkout();
        // Commands are part of the api module, which include git-like calls
        coCmd.setName("master");
        coCmd.setCreateBranch(false); // probably not needed, just to make sure
        coCmd.call(); // switch to "master" branch

        RemoteAddCommand remoteAddCommand = git.remoteAdd();
        remoteAddCommand.setName("origin");
        remoteAddCommand.setUri(new URIish("https://github.com/addy1219/Java.git"));
        remoteAddCommand.call();

        // git push -u origin master...

        PushCommand pushCommand = git.push();
        pushCommand.setCredentialsProvider(credentialsProvider);
        pushCommand.add("master");
        pushCommand.setRemote("origin");
        pushCommand.call();

        System.out.println("Pushed !");
    }

    public static void pull(Git git, CredentialsProvider credentialsProvider) throws GitAPIException {

        PullResult result = git.pull().setCredentialsProvider(credentialsProvider).call();
        System.out.println(result.isSuccessful());
    }

    public static void merge(Git git, CredentialsProvider credentialsProvider) throws GitAPIException {

        PushCommand pushCommand = git.push();
        pushCommand.setCredentialsProvider(credentialsProvider);
        pushCommand.add("master");
        pushCommand.setRemote("origin");
        Iterable<PushResult> pushResults = pushCommand.call();

        for (PushResult pushResult : pushResults) {
            for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
                System.out.println(update.getStatus());
            }
        }
        PullResult result = git.pull().setCredentialsProvider(credentialsProvider).call();
        FetchResult fetchResult = result.getFetchResult();
        MergeResult mergeResult = result.getMergeResult();
        MergeResult.MergeStatus mergeStatus = mergeResult.getMergeStatus();

        for (TrackingRefUpdate update : fetchResult.getTrackingRefUpdates()) {
            System.out.println(update.getLocalName());
            System.out.println(update.getRemoteName());
            System.out.println(update.getResult());
        }
        System.out.println(mergeResult);
        System.out.println(mergeStatus);

        pushResults = pushCommand.call();

        for (PushResult pushResult : pushResults) {
            for (RemoteRefUpdate update : pushResult.getRemoteUpdates()) {
                System.out.println(update.getStatus());
            }
        }
        System.out.println("Pulled !");
    }
    public static void newProject(GitLabApi gitLabApi) throws GitLabApiException {
        Project project = new Project()
                .withName("projectName")
                .withDescription("My project...")
                .withIssuesEnabled(true)
                .withMergeRequestsEnabled(true)
                .withWikiEnabled(true)
                .withSnippetsEnabled(true)
                .withPublic(false);
        Project newProject = gitLabApi.getProjectApi().createProject(project);
        System.out.println(newProject.getName());
    }
    public static void newUser(GitLabApi gitLabApi) throws GitLabApiException {
        User user = new User()
                .withName("name")
                .withUsername("username")
                .withEmail("email")
                .withCanCreateGroup(true)
                .withCanCreateProject(true)
                .withIsAdmin(false);
        gitLabApi.getUserApi().createUser(user,"password", true);
    }
    public static void branchOps(Git git, CredentialsProvider credentialsProvider) throws GitAPIException, URISyntaxException {
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        if(choice==1) {
            git.branchCreate().setName("newBranch").call();
            System.out.println("new branch created...");
        }
        else if(choice==2) {
            git.checkout().setName("newBranch").call();
            System.out.println("checked out...");
        }
        else if(choice==3) {
            git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().stream()
                    .anyMatch(branch -> branch.getName().equals("refs/remotes/origin/" + "branchName"));
            System.out.println("Branch exists or not");
        }
        else if(choice==4) {
            git.checkout().setName("master");
            git.branchDelete().setBranchNames("newBranch");
            System.out.println("Back on master branch and removed the created one...");
        }
        else if(choice==5) {
            System.out.println("Listing the existing branches...");
            List<Ref> call = git.branchList().call();
            for (Ref ref : call) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }
        else
            System.out.println("Wrong choice !");

        push(git, credentialsProvider);
    }

    public static void createTag(Git git, CredentialsProvider credentialsProvider) throws GitAPIException {
        String tagName = "tagName", tagMessage = "tagMessage";
        git.tag().setName(tagName).setMessage(tagMessage).setForceUpdate(true).call();
        git.push().setCredentialsProvider(credentialsProvider).setPushTags().call();

        System.out.println(git.tag().getName());
    }

    public static void hasLocalChanges(Git git) throws GitAPIException {
        Status status = git.status().call();

        Set<String> uncommittedChanges = status.getUncommittedChanges();
        if( uncommittedChanges.isEmpty() ) {
            System.out.println(false);
            return;
        }
        else
        {
            for( String uncommitted : uncommittedChanges )
              System.out.println(uncommitted);
        }
    }
}