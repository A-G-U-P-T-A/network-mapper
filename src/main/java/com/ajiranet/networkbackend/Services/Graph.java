package com.ajiranet.networkbackend.Services;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class Graph {
    private int V;
    private final LinkedList<Integer>[] adj;

    @Getter private final List<Integer>pathList = new ArrayList<>();

    Graph(int v)
    {
        V = v;
        adj = new LinkedList[v];
        for (int i = 0; i < v; ++i)
            adj[i] = new LinkedList<>();
    }

    void addEdge(int v, int w)
    {
        adj[v].add(w);
    }

    void DFSUtil(int v, boolean[] visited)
    {
        visited[v] = true;
        //System.out.print(v + " ");
        pathList.add(v);

        for (int n : adj[v]) {
            if (!visited[n])
                DFSUtil(n, visited);
        }
    }

    void DFS(int v)
    {
        boolean[] visited = new boolean[V];
        DFSUtil(v, visited);
    }

}