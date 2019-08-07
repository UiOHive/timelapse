import os
import dropbox

def areNewPicturesAvailable(ACCESS_TOKEN,path):

    dbx = dropbox.Dropbox(ACCESS_TOKEN)
    
    listOfFiles = []
    listFolderResult = dbx.files_list_folder(path)
    listOfFiles = list(set(listOfFiles+listFolderResult.entries))

    while(listFolderResult.has_more):
        listFolderResult = dbx.files_list_folder_continue(listFolderResult.cursor)
        listOfFiles = list(set(listOfFiles+
                               listFolderResult.entries))

    return listOfFiles
        
        


def downloadFile(ACCESS_TOKEN, pathOnDropbox,pathOnComputer):
    """
    param: ACCESS_TOKEN, string, access token to dropbox
    param: 
    """
    dbx = dropbox.Dropbox(ACCESS_TOKEN)

    with open(pathOnComputer, "wb") as f:
        metadata, res = dbx.files_download(path=pathOnDropbox)
        f.write(res.content)


def listLocalPicturesFile(ext,path):
    """
    param: ext, string, extension we're looking to list
    param: path, string, path to the directory we want to explore

    return: [string], list of files corsponding to the ext we're looking for in
    the directory
    """

    files = os.listdir(path)

    pictureFiles = []
    
    for f in files:
        if os.path.isfile(os.path.join(path,f)) and f.path.endwith(ext):
            pictureFiles.append(f)

    return pictureFiles

if __name__ == "__main__":
    from dropbox import DropboxOAuth2FlowNoRedirect

    APP_KEY = "*******"
    APP_SECRET = "********"

    auth_flow = DropboxOAuth2FlowNoRedirect(APP_KEY, APP_SECRET)

    authorize_url = auth_flow.start()
    print("1. Go to: " + authorize_url)
    print("2. Click \"Allow\" (you might have to log in first).")
    print("3. Copy the authorization code.")
    auth_code = input("Enter the authorization code here: ").strip()

    try:
        oauth_result = auth_flow.finish(auth_code)
    except Exception as e:
        print('Error: %s' % (e,))
    

    ACCESS_TOKEN = oauth_result.access_token

    l = areNewPicturesAvailable(ACCESS_TOKEN,"")
    print(l)        
