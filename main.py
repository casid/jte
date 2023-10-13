def define_env(env):
    @env.macro
    def github(filepath):
        version = env.conf["pom_env_vars"]["POM_VERSION"]
        repo_url = env.conf["repo_url"]
        return f"{repo_url}/blob/{version}/{filepath}"

    @env.macro
    def since(version):
        return f"!!! info \"Since when?\" \n\n    Available since jte ^^**{version}**^^."