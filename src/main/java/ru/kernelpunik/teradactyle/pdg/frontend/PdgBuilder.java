package ru.kernelpunik.teradactyle.pdg.frontend;

import org.springframework.stereotype.Component;
import ru.kernelpunik.teradactyle.pdg.model.Edge;
import ru.kernelpunik.teradactyle.pdg.model.Graph;
import ru.kernelpunik.teradactyle.pdg.model.Node;

import soot.*;
import soot.JastAddJ.Opt;
import soot.JastAddJ.Stmt;
import soot.options.Options;
import soot.toolkits.graph.pdg.EnhancedUnitGraph;
import soot.toolkits.graph.pdg.HashMutablePDG;
import soot.toolkits.graph.pdg.PDGNode;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Строит Program Dependence Graph (PDG) всего проекта при помощи Soot.
 * <p>
 * Замечание: для упрощения и во избежание рефлексии здесь не извлекаются
 * реальные номера строк исходного кода. Каждому узлу назначается
 * искусственный инкрементальный номер, пригодный для относительных
 * сравнений внутри одного анализа.
 */
@Component
public class PdgBuilder {

    /**
     * @param projectRoot директория с .class или .jar файлами
     * @return объединённый PDG проекта как {@link Graph}
     */
    public Graph build(String projectRoot) {

        String rt = "/opt/rt8/rt.jar";

        String cp = Options.v().soot_classpath();      // то, что Soot уже собрал
        cp += File.pathSeparator + rt;                 // добавляем rt.jar
        Options.v().set_soot_classpath(cp);
        String boot = System.getProperty("sun.boot.class.path");
        if (boot == null) {
            System.setProperty("sun.boot.class.path", System.getProperty("java.class.path"));
        }
        if (System.getProperty("java.ext.dirs") == null) {
            System.setProperty("java.ext.dirs", "");
        }
        Options.v().set_prepend_classpath(true);
        G.reset();
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_ignore_classpath_errors(true);        // 1. Подготовка Soot
        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(projectRoot));
        Options.v().set_src_prec(Options.src_prec_class);
        Options.v().set_output_format(Options.output_format_none);
        Options.v().set_keep_line_number(true);
        PackManager.v().runPacks();

        // 2. Аккумулируем PDG всех методов
        Map<PDGNode, Node> map = new HashMap<>();
        Set<Node> nodes = new HashSet<>();
        Set<Edge> edges = new HashSet<>();
        AtomicInteger artificialLine = new AtomicInteger(0);

        for (SootClass sc : Scene.v().getApplicationClasses()) {
            for (SootMethod m : sc.getMethods()) {
                if (!m.isConcrete()) continue;
                Body body;
                try {
                    body = m.retrieveActiveBody();
                } catch (RuntimeException ex) {
                    continue;
                }

                EnhancedUnitGraph cfg = new EnhancedUnitGraph(body);
                HashMutablePDG pdg = new HashMutablePDG(cfg);

                // Узлы
                for (PDGNode pn : pdg) {
                    String label = pn.getType().toString(); 
                    Node n = map.computeIfAbsent(pn, p -> new Node(
                            label,
                            label,
                            artificialLine.getAndIncrement()
                    ));
                    nodes.add(n);
                }

                // Рёбра зависимостей
                for (PDGNode src : pdg) {
                    for (Object succObj : pdg.getSuccsOf(src)) {
                        PDGNode tgt = (PDGNode) succObj;
                        Node from = map.get(src);
                        Node to = map.get(tgt);
                        edges.add(new Edge(from, to, "dependency"));
                    }
                }
            }
        }
        return new Graph(nodes, edges);
    }
}
