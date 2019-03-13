dmNames{1} = 'Dtw';
dmNames{2} = 'Ddtw';
dmNames{3} = 'Wdtw';
dmNames{4} = 'Wddtw';
dmNames{5} = 'Lcss';
dmNames{6} = 'Msm';
dmNames{7} = 'Twe';
dmNames{8} = 'Erp';
datasetNames = {"ArrowHead",
                "Beef",
                "BeetleFly",
                "BirdChicken",
                "CBF",
                "Car",
                "Coffee",
                "DiatomSizeReduction",
                "ECG200",
                "ECGFiveDays",
                "FaceFour",
                "GunPoint",
                "Herring",
                "ItalyPowerDemand",
                "Lightning2",
                "Lightning7",
                "Meat",
                "MoteStrain",
                "OliveOil",
                "Plane",
                "ShapeletSim",
                "SonyAIBORobotSurface1",
                "SonyAIBORobotSurface2",
                "Symbols",
                "SyntheticControl",
                "ToeSegmentation1",
                "ToeSegmentation2",
                "Trace",
                "TwoLeadECG",
                "Wine"};
relInst = true;
relScore = true;
instMethod = 'sampledTrainInstances';
scoreName = 'balAcc';
script = str2func(scoreName);
script();
% if exist('d', 'var') == 0
%     script();
% end
if relInst
    instMethod = strcat('rel', instMethod);
end
if relScore
    scoreName = strcat('rel', scoreName);
end
version = 'paramPath';
for dm = 1 : size(d, 3)
%     outDir = strcat(version, "/", instMethod, "/", scoreName);
%     mkdir(outDir);
%     fig = figure;
%     figName = strcat(dmNames{dm}, 'Train');
%     title(figName);
%     xlabel(instMethod);
%     ylabel(scoreName);
%     hold on;
%     indexes = cell(size(d, 2), 1);
%     for dataset = 1 : size(d, 2)
%         data = d{1, dataset, dm};
%         analysedResults = max(data);
%         indexes{dataset} = zeros(1, size(data, 2));
%         for i = 1 : size(data, 2)
%             result = find(data(:, i) == analysedResults(i));
%             indexes{dataset}(i) = result(1);
%         end
%         if relScore
%             maximum = max(analysedResults, [], 'all');
%             minimum = min(analysedResults, [], 'all');
%             analysedResults = (analysedResults - minimum) / (maximum - minimum);
%         end
%         if ~relInst
%             x = (0 : size(data, 2) - 1);
%         else
%             x = (1 : size(data, 2)) / size(data, 2);
%         end
%         x = repmat(x, size(data, 1), 1);
%         plot(x(1,:), analysedResults);
%     end
%     hold off;
%     saveas(fig, strcat(outDir, '/', figName));
%     close(fig);
%     fig = figure;
%     figName = strcat(dmNames{dm}, 'Test');
%     title(figName);
%     xlabel(instMethod);
%     ylabel(scoreName);
%     hold on;
%     for dataset = 1 : size(d, 2)
%         data = d{2, dataset, dm};
%         analysedResults = zeros(1, size(data, 2));
%         for i = 1 : size(data, 2)
%             analysedResults(i) = data(indexes{dataset}(i), i);
%         end
%         if relScore
%             maximum = max(analysedResults, [], 'all');
%             minimum = min(analysedResults, [], 'all');
%             analysedResults = (analysedResults - minimum) / (maximum - minimum);
%         end
%         if ~relInst
%             x = (0 : size(data, 2) - 1);
%         else
%             x = (1 : size(data, 2)) / size(data, 2);
%         end
%         x = repmat(x, size(data, 1), 1);
%         plot(x(1,:), analysedResults);
% %         stdDev = std(data);
% %         errorbar(x(1,:), analysedResults, stdDev);
%     end
%     hold off;
%     saveas(fig, strcat(outDir, '/', figName));
%     close(fig);
%     fig = figure;
%     figName = strcat(dmNames{dm}, 'Train');
%     title(figName);
%     xlabel(instMethod);
%     ylabel("param");
%     hold on;
%     indexes = cell(size(d, 2), 1);
    fig = figure;
    for dataset = 1 : size(d, 2)
        datasetName = datasetNames{dataset};
        outDir = strcat(version, "/", dmNames{dm}, "/" , datasetName);
        mkdir(outDir);
        for trainOrTest = 1 : 2
            if trainOrTest == 1
                run = 'train';
            else
                run = 'test';
            end
            data = d{trainOrTest, dataset, dm};
            surf(data);
            xlabel('proportion of train instances');
            ylabel('parameter set id');
            zlabel('accuracy');
            title(strcat(datasetName, " ", run));
            saveas(fig, strcat(outDir, "/", run));
        end
%         analysedResults = max(data);
%         indexes{dataset} = zeros(1, size(data, 2));
%         for i = 1 : size(data, 2)
%             result = find(data(:, i) == analysedResults(i));
%             indexes{dataset}(i) = result(1);
%             for j = 1 : size(result, 1)
%                 scatter(i, result(j));
%             end
%         end
        
        
        
%         if relScore
%             maximum = max(analysedResults, [], 'all');
%             minimum = min(analysedResults, [], 'all');
%             analysedResults = (analysedResults - minimum) / (maximum - minimum);
%         end
%         if ~relInst
%             x = (0 : size(data, 2) - 1);
%         else
%             x = (1 : size(data, 2)) / size(data, 2);
%         end
%         x = repmat(x, size(data, 1), 1);
%         plot(x(1,:), analysedResults);
    end
%     hold off;
%     saveas(fig, strcat(outDir, '/', figName));
    close(fig);
end       